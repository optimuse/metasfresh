/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2007 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
/** Generated Model - DO NOT CHANGE */
package de.metas.invoicecandidate.model;

import java.sql.ResultSet;
import java.util.Properties;

/** Generated Model for C_ILCandHandler
 *  @author Adempiere (generated) 
 */
@SuppressWarnings("javadoc")
public class X_C_ILCandHandler extends org.compiere.model.PO implements I_C_ILCandHandler, org.compiere.model.I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = -403206799L;

    /** Standard Constructor */
    public X_C_ILCandHandler (Properties ctx, int C_ILCandHandler_ID, String trxName)
    {
      super (ctx, C_ILCandHandler_ID, trxName);
      /** if (C_ILCandHandler_ID == 0)
        {
			setAD_User_InCharge_ID (0);
			setC_ILCandHandler_ID (0);
			setClassname (null);
			setEntityType (null);
// de.metas.invoicecandidate
			setIs_AD_User_InCharge_UI_Setting (false);
// N
			setName (null);
			setTableName (null);
        } */
    }

    /** Load Constructor */
    public X_C_ILCandHandler (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }


    /** Load Meta Data */
    @Override
    protected org.compiere.model.POInfo initPO (Properties ctx)
    {
      org.compiere.model.POInfo poi = org.compiere.model.POInfo.getPOInfo (ctx, Table_Name, get_TrxName());
      return poi;
    }

	@Override
	public org.compiere.model.I_AD_User getAD_User_InCharge() throws RuntimeException
	{
		return get_ValueAsPO(COLUMNNAME_AD_User_InCharge_ID, org.compiere.model.I_AD_User.class);
	}

	@Override
	public void setAD_User_InCharge(org.compiere.model.I_AD_User AD_User_InCharge)
	{
		set_ValueFromPO(COLUMNNAME_AD_User_InCharge_ID, org.compiere.model.I_AD_User.class, AD_User_InCharge);
	}

	/** Set Betreuer.
		@param AD_User_InCharge_ID 
		Person, die bei einem fachlichen Problem vom System informiert wird.
	  */
	@Override
	public void setAD_User_InCharge_ID (int AD_User_InCharge_ID)
	{
		if (AD_User_InCharge_ID < 1) 
			set_Value (COLUMNNAME_AD_User_InCharge_ID, null);
		else 
			set_Value (COLUMNNAME_AD_User_InCharge_ID, Integer.valueOf(AD_User_InCharge_ID));
	}

	/** Get Betreuer.
		@return Person, die bei einem fachlichen Problem vom System informiert wird.
	  */
	@Override
	public int getAD_User_InCharge_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_User_InCharge_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Rechnungskandidaten-Controller.
		@param C_ILCandHandler_ID Rechnungskandidaten-Controller	  */
	@Override
	public void setC_ILCandHandler_ID (int C_ILCandHandler_ID)
	{
		if (C_ILCandHandler_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_C_ILCandHandler_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_C_ILCandHandler_ID, Integer.valueOf(C_ILCandHandler_ID));
	}

	/** Get Rechnungskandidaten-Controller.
		@return Rechnungskandidaten-Controller	  */
	@Override
	public int getC_ILCandHandler_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_ILCandHandler_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Java-Klasse.
		@param Classname Java-Klasse	  */
	@Override
	public void setClassname (java.lang.String Classname)
	{
		set_Value (COLUMNNAME_Classname, Classname);
	}

	/** Get Java-Klasse.
		@return Java-Klasse	  */
	@Override
	public java.lang.String getClassname () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_Classname);
	}

	/** Set Beschreibung.
		@param Description Beschreibung	  */
	@Override
	public void setDescription (java.lang.String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Beschreibung.
		@return Beschreibung	  */
	@Override
	public java.lang.String getDescription () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_Description);
	}

	/** 
	 * EntityType AD_Reference_ID=389
	 * Reference name: _EntityTypeNew
	 */
	public static final int ENTITYTYPE_AD_Reference_ID=389;
	/** Set Entitäts-Art.
		@param EntityType 
		Dictionary Entity Type; Determines ownership and synchronization
	  */
	@Override
	public void setEntityType (java.lang.String EntityType)
	{

		set_Value (COLUMNNAME_EntityType, EntityType);
	}

	/** Get Entitäts-Art.
		@return Dictionary Entity Type; Determines ownership and synchronization
	  */
	@Override
	public java.lang.String getEntityType () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_EntityType);
	}

	/** Set Betreuer ist Benutzer-Editierbar.
		@param Is_AD_User_InCharge_UI_Setting 
		Sagt aus, ob der Berteuer durch den Benutzer eingestelltwerden soll oder nicht
	  */
	@Override
	public void setIs_AD_User_InCharge_UI_Setting (boolean Is_AD_User_InCharge_UI_Setting)
	{
		set_Value (COLUMNNAME_Is_AD_User_InCharge_UI_Setting, Boolean.valueOf(Is_AD_User_InCharge_UI_Setting));
	}

	/** Get Betreuer ist Benutzer-Editierbar.
		@return Sagt aus, ob der Berteuer durch den Benutzer eingestelltwerden soll oder nicht
	  */
	@Override
	public boolean is_AD_User_InCharge_UI_Setting () 
	{
		Object oo = get_Value(COLUMNNAME_Is_AD_User_InCharge_UI_Setting);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Name.
		@param Name 
		Alphanumeric identifier of the entity
	  */
	@Override
	public void setName (java.lang.String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Alphanumeric identifier of the entity
	  */
	@Override
	public java.lang.String getName () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_Name);
	}

	/** Set Name der DB-Tabelle.
		@param TableName Name der DB-Tabelle	  */
	@Override
	public void setTableName (java.lang.String TableName)
	{
		set_Value (COLUMNNAME_TableName, TableName);
	}

	/** Get Name der DB-Tabelle.
		@return Name der DB-Tabelle	  */
	@Override
	public java.lang.String getTableName () 
	{
		return (java.lang.String)get_Value(COLUMNNAME_TableName);
	}
}