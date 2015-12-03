package de.metas.materialtracking.qualityBasedInvoicing.ic.spi.impl;

/*
 * #%L
 * de.metas.materialtracking
 * %%
 * Copyright (C) 2015 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.adempiere.model.IContextAware;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.pricing.api.IPricingResult;
import org.adempiere.util.Check;
import org.adempiere.util.ILoggable;
import org.adempiere.util.Services;
import org.compiere.model.I_C_Activity;
import org.compiere.model.I_C_UOM;
import org.compiere.model.I_M_PriceList_Version;
import org.compiere.model.I_M_Product;
import org.compiere.model.I_M_Warehouse;
import org.compiere.util.Env;

import com.google.common.annotations.VisibleForTesting;

import de.metas.flatrate.api.IFlatrateDB;
import de.metas.flatrate.model.I_C_Invoice_Clearing_Alloc;
import de.metas.invoicecandidate.api.IInvoiceCandDAO;
import de.metas.invoicecandidate.model.I_C_ILCandHandler;
import de.metas.materialtracking.model.I_C_Invoice_Candidate;
import de.metas.materialtracking.model.I_PP_Order;
import de.metas.materialtracking.qualityBasedInvoicing.IMaterialTrackingDocuments;
import de.metas.materialtracking.qualityBasedInvoicing.IQualityInspectionOrder;
import de.metas.materialtracking.qualityBasedInvoicing.IVendorInvoicingInfo;
import de.metas.materialtracking.qualityBasedInvoicing.invoicing.IQualityInvoiceLine;
import de.metas.materialtracking.qualityBasedInvoicing.invoicing.IQualityInvoiceLineGroup;
import de.metas.materialtracking.qualityBasedInvoicing.invoicing.QualityInvoiceLineGroupByTypeComparator;
import de.metas.materialtracking.qualityBasedInvoicing.invoicing.QualityInvoiceLineGroupType;
import de.metas.product.acct.api.IProductAcctDAO;
import de.metas.tax.api.ITaxBL;

/**
 * Takes {@link IQualityInvoiceLineGroup}s and creates {@link I_C_Invoice_Candidate}s.
 *
 * @author tsa
 *
 */
public class InvoiceCandidateWriter
{
	// Services
	private final ITaxBL taxBL = Services.get(ITaxBL.class);
	private final IProductAcctDAO productAcctDAO = Services.get(IProductAcctDAO.class);

	// Parameters:
	private final IContextAware _context;
	private I_C_ILCandHandler _invoiceCandidateHandler;
	private IVendorInvoicingInfo _vendorInvoicingInfo;
	private int invoiceDocTypeId = -1;
	/**
	 * Original invoice candidates that need to be cleared when a new invoice candidate is created by this builder.
	 *
	 * NOTE: please don't use this object in other scope then clearing. If you want more invoicing informations, please take them from {@link #_vendorInvoicingInfo}.
	 */
	private List<I_C_Invoice_Candidate> _invoiceCandidatesToClear;
	private QualityInvoiceLineGroupByTypeComparator groupsSorter;

	// Current Status:
	private IMaterialTrackingDocuments _materialTrackingDocuments = null;
	private IQualityInspectionOrder _qualityInspectionOrder;

	private int _lineNoNext = 10;

	// Result
	private final List<I_C_Invoice_Candidate> _createdInvoiceCandidates = new ArrayList<>();

	public InvoiceCandidateWriter(final IContextAware context)
	{
		super();

		Check.assumeNotNull(context, "context not null");
		_context = context;
	}

	public InvoiceCandidateWriter setC_ILCandHandler(final I_C_ILCandHandler invoiceCandidateHandler)
	{
		_invoiceCandidateHandler = invoiceCandidateHandler;
		return this;
	}

	private I_C_ILCandHandler getC_ILCandHandler()
	{
		Check.assumeNotNull(_invoiceCandidateHandler, "invoiceCandidateHandler not null");
		return _invoiceCandidateHandler;
	}

	/**
	 * Sets the original invoice candidates that need to be cleared when a new invoice candidate is created by this builder
	 *
	 * @param invoiceCandidateToClear
	 */
	public InvoiceCandidateWriter setInvoiceCandidatesToClear(final List<I_C_Invoice_Candidate> invoiceCandidatesToClear)
	{
		_invoiceCandidatesToClear = invoiceCandidatesToClear;
		return this;
	}

	/**
	 * Sets which group types will be accepted and saved.
	 *
	 * Also, the {@link IQualityInvoiceLineGroup}s will be sorted exactly by the order of given types.
	 *
	 * @param types
	 */
	public InvoiceCandidateWriter setQualityInvoiceLineGroupTypes(final List<QualityInvoiceLineGroupType> types)
	{
		if (types == null)
		{
			groupsSorter = null;
		}
		else
		{
			groupsSorter = new QualityInvoiceLineGroupByTypeComparator(types);
		}
		return this;
	}

	/**
	 * Sets C_DocType_ID to be used when invoice will be created
	 *
	 * @param invoiceDocTypeId
	 */
	public void setInvoiceDocType_ID(final int invoiceDocTypeId)
	{
		this.invoiceDocTypeId = invoiceDocTypeId;
	}

	/**
	 * @return created invoice candidates
	 */
	public List<I_C_Invoice_Candidate> getCreatedInvoiceCandidates()
	{
		return new ArrayList<>(_createdInvoiceCandidates);
	}

	private final void addToCreatedInvoiceCandidates(final I_C_Invoice_Candidate invoiceCandidate)
	{
		Check.assumeNotNull(invoiceCandidate, "invoiceCandidate not null");

		final boolean newIc = InterfaceWrapperHelper.isNew(invoiceCandidate);

		//
		// Make sure the invoice candidate is saved, so that we can use its ID further down (e.g. to reference if from the detail lines)
		InterfaceWrapperHelper.save(invoiceCandidate);

		ILoggable.THREADLOCAL.getLoggableOr(ILoggable.NULL).addLog(newIc ? "Created new IC " : "Updated existing IC " + invoiceCandidate);

		final IFlatrateDB flatrateDB = Services.get(IFlatrateDB.class);

		//
		// delete/recreate Invoice Clearing Allocation
		Check.assumeNotNull(_invoiceCandidatesToClear, "_invoiceCandidateToClear not null");
		for (final I_C_Invoice_Candidate invoiceCandidateToClear : _invoiceCandidatesToClear)
		{
			final IVendorInvoicingInfo vendorInvoicingInfo = getVendorInvoicingInfo();

			for (I_C_Invoice_Clearing_Alloc ica : flatrateDB.retrieveClearingAllocs(invoiceCandidate))
			{
				InterfaceWrapperHelper.delete(ica);
			}

			final I_C_Invoice_Clearing_Alloc newIca = InterfaceWrapperHelper.newInstance(I_C_Invoice_Clearing_Alloc.class, getContext());
			newIca.setAD_Org_ID(invoiceCandidate.getAD_Org_ID()); // workaround, the correct AD_Org_ID should already be there from getContext()
			newIca.setC_Flatrate_Term(vendorInvoicingInfo.getC_Flatrate_Term());
			newIca.setC_Invoice_Cand_ToClear(invoiceCandidateToClear);
			newIca.setC_Invoice_Candidate(invoiceCandidate);
			InterfaceWrapperHelper.save(newIca);
		}

		//
		// Link to material tracking
		{
			final IMaterialTrackingDocuments materialTrackingDocuments = getMaterialTrackingDocuments();
			materialTrackingDocuments.linkModelToMaterialTracking(invoiceCandidate);
		}

		// add to created list
		_createdInvoiceCandidates.add(invoiceCandidate);

		// increment next line number to be used
		_lineNoNext += 10;
	}

	public InvoiceCandidateWriter setVendorInvoicingInfo(final IVendorInvoicingInfo vendorInvoicingInfo)
	{
		_vendorInvoicingInfo = vendorInvoicingInfo;
		return this;
	}

	/**
	 * @return vendor invoicing info; never return null
	 */
	private final IVendorInvoicingInfo getVendorInvoicingInfo()
	{
		Check.assumeNotNull(_vendorInvoicingInfo, "_vendorInvoicingInfo not null");
		return _vendorInvoicingInfo;
	}

	private final IContextAware getContext()
	{
		return _context;
	}

	private IMaterialTrackingDocuments getMaterialTrackingDocuments()
	{
		Check.assumeNotNull(_materialTrackingDocuments, "_materialTrackingDocuments not null");
		return _materialTrackingDocuments;
	}

	public InvoiceCandidateWriter setMaterialTrackingDocuments(final IMaterialTrackingDocuments materialTrackingDocuments)
	{
		_materialTrackingDocuments = materialTrackingDocuments;
		return this;
	}

	private IQualityInspectionOrder getQualityInspectionOrder()
	{
		Check.assumeNotNull(_qualityInspectionOrder, "_qualityInspectionOrder not null");
		return _qualityInspectionOrder;
	}

	public InvoiceCandidateWriter setQualityInspectionOrder(final IQualityInspectionOrder qualityInspectionOrder)
	{
		_qualityInspectionOrder = qualityInspectionOrder;
		return this;
	}

	public InvoiceCandidateWriter create(final List<IQualityInvoiceLineGroup> qualityInvoiceLineGroups)
	{
		if (qualityInvoiceLineGroups == null || qualityInvoiceLineGroups.isEmpty())
		{
			return this;
		}

		//
		// Discard not accepted groups and then sort them
		final List<IQualityInvoiceLineGroup> qualityInvoiceLineGroupsToProcess = new ArrayList<>(qualityInvoiceLineGroups);
		if (groupsSorter != null)
		{
			groupsSorter.filterAndSort(qualityInvoiceLineGroupsToProcess);
		}

		//
		// Iterate groups and create invoice candidates
		for (final IQualityInvoiceLineGroup qualityInvoiceLineGroup : qualityInvoiceLineGroupsToProcess)
		{
			create(qualityInvoiceLineGroup);
		}

		return this;
	}

	private void create(final IQualityInvoiceLineGroup qualityInvoiceLineGroup)
	{
		Check.assumeNotNull(qualityInvoiceLineGroup, "qualityInvoiceLineGroup not null");

		//
		// Create Invoice Candidate from invoiceable line
		final IQualityInvoiceLine invoiceableLine = qualityInvoiceLineGroup.getInvoiceableLine();
		final I_C_Invoice_Candidate invoiceCandidate = createOrUpdateInvoiceCandidate(invoiceableLine);

		Services.get(IInvoiceCandDAO.class).deleteInvoiceDetails(invoiceCandidate);

		if (qualityInvoiceLineGroup.getInvoiceableLineOverride() != null)
		{
			final IQualityInvoiceLine invoiceableLineOverride = qualityInvoiceLineGroup.getInvoiceableLineOverride();
			final InvoiceDetailWriter detailsWriter = new InvoiceDetailWriter(invoiceCandidate);
			detailsWriter.setPrintOverride(true);
			detailsWriter.saveLines(Collections.singletonList(invoiceableLineOverride));
		}

		//
		// Before details
		{
			final InvoiceDetailWriter detailsWriter = new InvoiceDetailWriter(invoiceCandidate);
			detailsWriter.setPrintBefore(true);
			detailsWriter.saveLines(qualityInvoiceLineGroup.getDetailsBefore());
		}

		//
		// After details
		{
			final InvoiceDetailWriter detailsWriter = new InvoiceDetailWriter(invoiceCandidate);
			detailsWriter.setPrintBefore(false); // i.e. after
			detailsWriter.saveLines(qualityInvoiceLineGroup.getDetailsAfter());
		}
	}

	/**
	 * Creates invoice candidate
	 *
	 * @param invoiceableLine
	 * @return invoice candidate; never returns <code>null</code>
	 */
	private I_C_Invoice_Candidate createOrUpdateInvoiceCandidate(final IQualityInvoiceLine invoiceableLine)
	{
		Check.assumeNotNull(invoiceableLine, "invoiceableLine not null");

		//
		// Extract infos from Order
		final IQualityInspectionOrder order = getQualityInspectionOrder();
		final I_PP_Order ppOrder = order.getPP_Order();
		// final Timestamp dateOfProduction = order.getDateOfProduction();
		final int orgId = ppOrder.getAD_Org_ID();
		final int modelTableId = InterfaceWrapperHelper.getModelTableId(ppOrder);
		final int modelRecordId = ppOrder.getPP_Order_ID();

		//
		// Extract infos from invoiceable line
		final I_M_Product product = invoiceableLine.getM_Product();
		final I_C_UOM uom = invoiceableLine.getC_UOM();
		final BigDecimal qtyOrdered = invoiceableLine.getQty();
		final String description = invoiceableLine.getDescription();
		final IPricingResult pricingResult = invoiceableLine.getPrice();
		final boolean printed = invoiceableLine.isDisplayed();

		//
		// Invoicing config
		final IVendorInvoicingInfo vendorInvoicingInfo = getVendorInvoicingInfo();
		final I_M_PriceList_Version priceListVersion = vendorInvoicingInfo.getM_PriceList_Version();
		Check.errorUnless(priceListVersion.getM_PriceList_Version_ID() == pricingResult.getM_PriceList_Version_ID(),
				"The plv of vendorInvoicingInfo {0} shall have the same M_PriceList_Version_ID as the pricingResult {1} of the current invoiceableLine {2}",
				vendorInvoicingInfo, pricingResult, invoiceableLine);

		final I_C_Invoice_Candidate ic = InterfaceWrapperHelper.newInstance(I_C_Invoice_Candidate.class, getContext());

		//
		// Create the new Invoice Candidate
		//
		// NOTE: don't link these invoice candidates to C_OrderLine because in that case
		// de.metas.materialtracking.qualityBasedInvoicing.IMaterialTrackingDocuments.getOriginalInvoiceCandidates()
		// will consider them; and we really don't want!

		if (invoiceDocTypeId > 0)
		{
			ic.setC_DocTypeInvoice_ID(invoiceDocTypeId);
		}
		ic.setAD_Org_ID(orgId);
		ic.setC_ILCandHandler(getC_ILCandHandler());
		ic.setIsSOTrx(false);
		ic.setDescription(description);
		ic.setIsPrinted(printed);

		// document reference
		ic.setAD_Table_ID(modelTableId);
		ic.setRecord_ID(modelRecordId);

		// product
		ic.setM_Product(product);

		// charge
		// int chargeId = olc.getC_Charge_ID();
		// ic.setC_Charge_ID(chargeId);

		ic.setQtyOrdered(qtyOrdered);
		ic.setQtyToInvoice(Env.ZERO); // to be computed
		ic.setC_UOM(uom);

		ic.setDateOrdered(order.getDateOfProduction());

		// bill partner data
		ic.setBill_BPartner_ID(vendorInvoicingInfo.getBill_BPartner_ID());
		ic.setBill_Location_ID(vendorInvoicingInfo.getBill_Location_ID());
		ic.setBill_User_ID(vendorInvoicingInfo.getBill_User_ID());

		//
		// Pricing
		ic.setM_PricingSystem_ID(pricingResult.getM_PricingSystem_ID());
		ic.setM_PriceList_Version_ID(pricingResult.getM_PriceList_Version_ID());
		ic.setPrice_UOM_ID(pricingResult.getPrice_UOM_ID());
		ic.setPriceEntered(pricingResult.getPriceStd());
		ic.setPriceActual(pricingResult.getPriceStd());
		ic.setIsTaxIncluded(pricingResult.isTaxIncluded()); // 08457: Configure new IC from pricing result
		ic.setDiscount(pricingResult.getDiscount());
		ic.setC_Currency_ID(pricingResult.getC_Currency_ID());

		// InvoiceRule
		ic.setInvoiceRule(vendorInvoicingInfo.getInvoiceRule());

		// Activity
		setC_Activity_ID(ic);

		// Tax
		setC_Tax_ID(ic, pricingResult, priceListVersion.getValidFrom());

		// Set invoice candidate's Line number
		ic.setLine(_lineNoNext);

		// Bind Material Tracking to the invoice candidate
		final IMaterialTrackingDocuments materialTrackingDocuments = getMaterialTrackingDocuments();
		ic.setM_Material_Tracking(materialTrackingDocuments.getM_Material_Tracking());

		// NOTE: don't save it

		// Add it to the list of created invoice candidates
		addToCreatedInvoiceCandidates(ic);

		// Return it
		return ic;
	}

	/**
	 *
	 * @param invoiceCandidate
	 */
	@VisibleForTesting
	protected void setC_Activity_ID(final I_C_Invoice_Candidate invoiceCandidate)
	{
		// 07442 activity
		final IContextAware context = getContext();
		final I_C_Activity activity = productAcctDAO.retrieveActivityForAcct(context, invoiceCandidate.getAD_Org(), invoiceCandidate.getM_Product());
		invoiceCandidate.setC_Activity(activity);
	}

	/**
	 *
	 * @param ic
	 * @param pricingResult
	 * @param date
	 */
	@VisibleForTesting
	protected void setC_Tax_ID(final I_C_Invoice_Candidate ic, final IPricingResult pricingResult, final Timestamp date)
	{
		final IContextAware contextProvider = getContext();

		final Properties ctx = contextProvider.getCtx();
		final String trxName = contextProvider.getTrxName();

		final int taxCategoryId = pricingResult.getC_TaxCategory_ID();
		final I_M_Warehouse warehouse = null; // warehouse: N/A
		final boolean isSOTrx = false;

		final int taxID = taxBL.getTax(
				ctx
				, ic
				, taxCategoryId
				, ic.getM_Product_ID()
				, -1 // C_Charge_ID
				, date // bill date
				, date // ship date
				, ic.getAD_Org_ID()
				, warehouse
				, ic.getBill_Location_ID()
				, -1 // shipPartnerLocation
				, isSOTrx
				, trxName
				);
		ic.setC_Tax_ID(taxID);
	}
}