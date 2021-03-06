/**
 *
 */
package de.metas.async.api;

/*
 * #%L
 * de.metas.async
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


import org.adempiere.util.ISingletonService;

import de.metas.async.model.I_C_Async_Batch;
import de.metas.async.model.I_C_Queue_WorkPackage;

/**
 * @author cg
 *
 */
public interface IAsyncBatchBL extends ISingletonService
{
	String AD_SYSCONFIG_ASYNC_BOILERPLATE_ID = "de.metas.async.api.IAsyncBatchBL.BoilerPlate_ID";

	/**
	 * @return {@link I_C_Async_Batch} builder
	 */
	IAsyncBatchBuilder newAsyncBatch();

	/**
	 * update first enqueued, last enqueued and count Enqueued
	 *
	 * @param qw
	 */
	void increaseEnqueued(final I_C_Queue_WorkPackage workPackage);

	/**
	 * update last processed and count processed
	 *
	 * @param qw
	 */
	void increaseProcessed(final I_C_Queue_WorkPackage workPackage);

	/**
	 * Update the processed-flag based on first enqueued, last enqueued, last processed, count enqueued and count processed.
	 *
	 * NOTE: this method it is also saving the updated batch.
	 *
	 * @param asyncBatch
	 */
	void updateProcessed(final I_C_Async_Batch asyncBatch);

	/**
	 * Enqueue batch for the de.metas.async.processor.impl.CheckProcessedAsynBatchWorkpackageProcessor processor. Call
	 * {@link IWorkPackageQueue#enqueueWorkPackage(de.metas.async.model.I_C_Queue_Block, String)} with priority = <code>null</code>. This is OK because we assume that there is a dedicated queue/thread
	 * for CheckProcessedAsynBatchWorkpackageProcessor
	 *
	 * @param asyncBatch
	 */
	void enqueueAsyncBatch(I_C_Async_Batch asyncBatch);

	// ts: commented out, because it's currently not called and because it uses business logic that was move to de.metas.business
	// if we need to use this again, then pls create or register a listener, or check if INotificationBL can be used or extended instead.
	/***
	 * Send mail to the user who created the async batch with the result based the on boiler plate the ID of which is defined by {@value #AD_SYSCONFIG_ASYNC_BOILERPLATE_ID}. If there is no such
	 * AS_SysConfig or no <code>AD_BoilerPlate</code> record, then the method does nothing.
	 *
	 * @param asyncBatch
	 * @see de.metas.letters.model.MADBoilerPlate#sendEMail(de.metas.letters.model.IEMailEditor, boolean)
	 */
	// void sendEMail(final I_C_Async_Batch asyncBatch);
}
