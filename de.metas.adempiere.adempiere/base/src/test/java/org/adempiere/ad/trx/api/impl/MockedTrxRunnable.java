package org.adempiere.ad.trx.api.impl;

/*
 * #%L
 * de.metas.adempiere.adempiere.base
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


import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.ad.trx.api.ITrxManager;
import org.adempiere.ad.trx.api.OnTrxMissingPolicy;
import org.adempiere.util.Check;
import org.compiere.util.TrxRunnableAdapter;
import org.junit.Assert;
import org.junit.Ignore;

@Ignore
public class MockedTrxRunnable extends TrxRunnableAdapter
{
	private final ITrxManager trxManager;
	private boolean executed = false;
	private String lastTrxName = null;
	private ITrx lastTrx = null;
	private Runnable runnable = null;

	private Runnable doFinallyRunnable = null;

	public MockedTrxRunnable(final ITrxManager trxManager)
	{
		super();

		Check.assumeNotNull(trxManager, "trxManager not null");
		this.trxManager = trxManager;
	}

	@Override
	public void run(final String localTrxName) throws Exception
	{
		Check.assume(!executed, "This runnable was already executed before: " + this);

		// NOTE: don't validate if localTrxName is null because we are just collecting informations here.
		this.lastTrxName = localTrxName;

		// NOTE: don't fail if transaction was not found... we shall allow the caller test to validate this, here we are just collecting informations
		this.lastTrx = trxManager.get(localTrxName, OnTrxMissingPolicy.ReturnTrxNone);

		// Execute the runnable if defined
		// NOTE: we are executing the runnable after we set the lastTrxName because some tests are relying on that.
		if (runnable != null)
		{
			runnable.run();
		}

		// Flag this Mocked TrxRunnable as executed
		this.executed = true;
	}

	@Override
	public void doFinally()
	{
		if (doFinallyRunnable != null)
		{
			doFinallyRunnable.run();
		}
	}

	public void assertExecuted()
	{
		Assert.assertEquals("Runnable was executed: " + this, true, this.executed);
	}

	public void assertExecutedUsingTrxName(final String expectedLastTrxName)
	{
		assertExecuted();
		Assert.assertEquals("Runnable was executed using expected trxName: " + this, expectedLastTrxName, this.lastTrxName);
	}

	/**
	 * Sets inner runnable to be executed on {@link #run(String)}
	 * 
	 * @param runnable
	 */
	public void setInnerRunnable(final Runnable runnable)
	{
		this.runnable = runnable;
	}

	public void setDoFinallyRunnable(final Runnable runnable)
	{
		this.doFinallyRunnable = runnable;
	}

	public boolean isExecuted()
	{
		return executed;
	}

	public String getLastTrxName()
	{
		return lastTrxName;
	}

	public ITrx getLastTrx()
	{
		return lastTrx;
	}
}
