package de.metas.adempiere.form.terminal;

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


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.adempiere.util.Check;

import de.metas.adempiere.form.terminal.context.ITerminalContext;
import de.metas.adempiere.form.terminal.context.ITerminalContextReferences;

public abstract class TerminalDialog implements ITerminalDialog
{
	private ITerminalFactory terminalFactory;
	private ITerminalContext terminalContext;
	private IComponent parent;
	private IComponent content;

	private ITerminalContextReferences contextReferences;
	private IConfirmPanel confirmPanel;
	private boolean canceled = false;
	private final CompositeTerminalDialogListener listeners = new CompositeTerminalDialogListener();
	private boolean initialized = false;

	public TerminalDialog(final ITerminalFactory terminalFactory, final IComponent parent, final IComponent content)
	{
		super();

		Check.assumeNotNull(terminalFactory, "terminalFactory not null");
		this.terminalFactory = terminalFactory;
		terminalContext = terminalFactory.getTerminalContext();

		this.parent = parent;
		this.content = content;
	}

	private final void init()
	{
		if (initialized)
		{
			return;
		}
		initialized = true;

		//
		// Create a new set of references from this point, to be able to destroy them when the dialog closes.
		// In this way we can avoid memory leaks right from this Dialog and from components, listeners etc that were created will using this dialog.
		contextReferences = terminalContext.newReferences();

		confirmPanel = terminalFactory.createConfirmPanel(true, ""); // withCancel=true
		confirmPanel.addListener(new PropertyChangeListener()
		{
			@Override
			public void propertyChange(final PropertyChangeEvent evt)
			{
				if (!IConfirmPanel.PROP_Action.equals(evt.getPropertyName()))
				{
					return;
				}
				final String action = String.valueOf(evt.getNewValue());
				if (IConfirmPanel.ACTION_OK.equals(action))
				{
					doOK();
				}
				else if (IConfirmPanel.ACTION_Cancel.equals(action))
				{
					doCancel();
				}
			}
		});

		if (content instanceof ITerminalDialogListener)
		{
			listeners.addListener((ITerminalDialogListener)content);
		}

		//
		// UI
		initComponentsUI();
		initLayoutUI();
	}

	protected abstract void initComponentsUI();

	protected abstract void initLayoutUI();

	public final ITerminalFactory getTerminalFactory()
	{
		return terminalFactory;
	}

	@Override
	public final ITerminalContext getTerminalContext()
	{
		return getTerminalFactory().getTerminalContext();
	}

	protected final IComponent getParent()
	{
		return parent;
	}

	protected final IComponent getContentPanel()
	{
		return content;
	}

	protected final IConfirmPanel getConfirmPanel()
	{
		return confirmPanel;
	}

	protected abstract void activateUI();

	@Override
	public final void activate()
	{
		init();
		activateUI();
	}

	protected final void doOK()
	{
		try
		{
			listeners.onDialogOk(this);
		}
		catch (final TerminalDialogCancelClosingException e)
		{
			// we are asked to cancel the dialog closing
			// so we do nothing, just return
			return;
		}

		//
		// Perform dialog closing with OK
		canceled = false;
		dispose();
	}

	protected final void doCancel()
	{
		boolean allowCanceling = true;
		try
		{
			allowCanceling = listeners.onDialogCanceling(this);
		}
		catch (final TerminalDialogCancelClosingException e)
		{
			// we are asked to cancel the dialog closing
			allowCanceling = false;

		}

		// stop here if we need to stop the canceling
		if (!allowCanceling)
		{
			return;
		}

		//
		// Perform canceling
		canceled = true;
		dispose();
	}

	boolean cancelDispose = false;

	@Override
	public final void cancelDispose()
	{
		cancelDispose = true;
	}

	@Override
	@OverridingMethodsMustInvokeSuper
	public final void dispose()
	{
		if (!cancelDispose)
		{
			disposeUI();

			confirmPanel = DisposableHelper.dispose(confirmPanel);
			content = DisposableHelper.dispose(content); // NOTE: we are also disposing the given content automatically
			listeners.dispose();
			parent = null;

			if (terminalContext != null)
			{
				terminalContext.deleteReferences(contextReferences);
				terminalContext = null;
			}
			contextReferences = null;
			terminalFactory = null;
		}
		cancelDispose = false;
	}

	protected abstract void disposeUI();

	@Override
	public final boolean isCanceled()
	{
		return canceled;
	}

	protected final void requestFocusOnComponentPanel()
	{
		final IComponent contentPanel = getContentPanel();
		if (contentPanel instanceof IFocusableComponent)
		{
			((IFocusableComponent)contentPanel).requestFocus();
		}
	}

	/**
	 * @see ITerminalDialogListener#onDialogOpened(ITerminalDialog)
	 */
	protected final void doOnDialogOpened()
	{
		listeners.onDialogOpened(this);
	}

	/**
	 * @see ITerminalDialogListener#onDialogActivated(ITerminalDialog)
	 */
	protected final void doOnDialogActivated()
	{
		listeners.onDialogActivated(this);
	}

	/**
	 * @see ITerminalDialogListener#onDialogDeactivated(ITerminalDialog)
	 */
	protected final void doOnDialogDeactivated()
	{
		listeners.onDialogDeactivated(this);
	}

	/**
	 * @see ITerminalDialogListener#onDialogClosing(ITerminalDialog)
	 */
	protected final void doOnDialogClosing()
	{
		listeners.onDialogClosing(this);
	}

	/**
	 * @see ITerminalDialogListener#onDialogClosed(ITerminalDialog)
	 */
	protected final void doOnDialogClosed()
	{
		listeners.onDialogClosed(this);
	}
}
