/**
 * 
 */
package de.metas.adempiere.form.terminal.context;

/*
 * #%L
 * de.metas.swat.base
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


import java.util.Properties;
import org.slf4j.Logger;
import de.metas.logging.LogManager;

import org.adempiere.util.Check;
import org.adempiere.util.collections.IdentityHashSet;
import org.adempiere.util.proxy.WeakWrapper;
import org.compiere.Adempiere;
import org.compiere.util.Env;

import de.metas.adempiere.form.terminal.ITerminalFactory;
import de.metas.adempiere.form.terminal.POSKeyLayout;
import de.metas.adempiere.form.terminal.swing.SwingTerminalFactory;

/**
 * @author tsa
 * 
 */
public class TerminalContextFactory
{
	private static final TerminalContextFactory instance = new TerminalContextFactory();

	public static TerminalContextFactory get()
	{
		return TerminalContextFactory.instance;
	}

	private final transient Logger logger = LogManager.getLogger(getClass());

	/* Internal list of created terminal contexts, to avoid being GCed */
	private final IdentityHashSet<ITerminalContext> terminalContexts = new IdentityHashSet<ITerminalContext>();

	public ITerminalContext createContext()
	{
		final TerminalContext terminalContext = new TerminalContext();

		// Set context
		final Properties ctx = Env.getCtx();
		terminalContext.setCtx(ctx);

		//
		// Setup components factory
		final ITerminalFactory factory = new SwingTerminalFactory(terminalContext); // TODO: HARDCODED
		terminalContext.setTerminalFactory(factory);

		// Set logged in user
		terminalContext.setAD_User_ID(Env.getAD_User_ID(ctx));

		// Set default font size to be used
		terminalContext.setDefaultFontSize(14);

		//
		// Setup standard keyboards
		if (!Adempiere.isUnitTestMode())
		{
			terminalContext.setTextKeyLayout(new POSKeyLayout(terminalContext, 540006)); // TODO HARDCODED Keyboard UPPERCASE (de_DE)
			terminalContext.setNumericKeyLayout(new POSKeyLayout(terminalContext, 50002)); // TODO: HARDCODED numeric pad
		}

		//
		// Add terminal context to current list of terminal contexts
		terminalContexts.add(terminalContext);

		// Return it
		return terminalContext;
	}

	public ITerminalContext copy(final ITerminalContext terminalContext)
	{
		Check.assumeNotNull(terminalContext, "terminalContext not null");

		final TerminalContext terminalContextNew = new TerminalContext();
		copyFrom(terminalContextNew, terminalContextNew);

		//
		// Add terminal context to current list of terminal contexts
		terminalContexts.add(terminalContextNew);

		// Return it
		return terminalContextNew;
	}

	private final void copyFrom(final TerminalContext terminalContextNew, final ITerminalContext terminalContext)
	{
		Check.assumeNotNull(terminalContext, "terminalContext not null");

		terminalContextNew.setCtx(terminalContext.getCtx());

		final ITerminalFactory terminalFactoryNew = terminalContext.getTerminalFactory().copy(terminalContextNew);
		terminalContextNew.setTerminalFactory(terminalFactoryNew);

		terminalContextNew.setNumericKeyLayout(terminalContext.getNumericKeyLayout());
		terminalContextNew.setTextKeyLayout(terminalContext.getTextKeyLayout());

		terminalContextNew.setWindowNo(terminalContext.getWindowNo());
		terminalContextNew.setAD_User_ID(terminalContext.getAD_User_ID());

		terminalContextNew.setDefaultFontSize(terminalContext.getDefaultFontSize());

		terminalContextNew.setScreenResolution(terminalContext.getScreenResolution());

		// services // don't copy them
		// hardReferences // don't copy them
		// propertyChangeSupports // don't copy them
	}

	public void destroy(final ITerminalContext terminalContext)
	{
		if (terminalContext == null)
		{
			return;
		}

		final ITerminalContext terminalContextUnwrapped = WeakWrapper.unwrap(terminalContext);
		Check.assumeNotNull(terminalContextUnwrapped, "terminalContextUnwrapped not null");

		terminalContextUnwrapped.dispose();

		if (!terminalContexts.remove(terminalContextUnwrapped))
		{
			if (!terminalContextUnwrapped.isDisposed())
			{
				logger.warn("Cannot remove {} from internal terminal contexts list because it was not found."
						+ "\nCurrent contexts are: {}"
						, new Object[] { terminalContext, terminalContexts });
			}
		}
	}
}
