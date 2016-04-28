package test.integration.commission;

/*
 * #%L
 * de.metas.commission.ait
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


import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import de.metas.adempiere.ait.test.IntegrationTestSuite;

@RunWith(IntegrationTestSuite.class)
//@IntegrationTestListeners({
//		test.integration.commission.bPartner.MBPartnerTestEventListener.class,
//		test.integration.commission.purchase.PurchaseTestEventListener.class,
//		test.integration.commission.sales.SalesTestAuxDriver.class,
//		test.integration.commission.sales.SalesTestEventListener.class
//})
@SuiteClasses({
		test.integration.commission.sponsor.HierarchyInsertionTests.class,
		test.integration.commission.sponsor.HierarchyChangeTests.class,
		test.integration.commission.bPartner.BPartnerLocationTest.class
})
public class AllTests
{
}