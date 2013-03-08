/*******************************************************************************
 * Copyright (c) 2009, 2011 Overture Team and others.
 *
 * Overture is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Overture is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Overture.  If not, see <http://www.gnu.org/licenses/>.
 * 	
 * The Overture Tool web-site: http://overturetool.org/
 *******************************************************************************/
package org.overture.ide.vdmpp.debug.ui.launchconfigurations;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.overture.ast.node.INode;
import org.overture.ide.debug.core.VdmDebugPlugin;
import org.overture.ide.debug.core.IDebugConstants;
import org.overture.ide.debug.ui.launchconfigurations.LauncherMessages;
import org.overture.ide.debug.ui.launchconfigurations.MethodSearchEngine;
import org.overture.ide.debug.ui.launchconfigurations.VdmLaunchShortcut;
import org.overture.ide.ui.utility.ast.AstNameUtil;
import org.overture.ide.vdmpp.debug.IVdmPpDebugConstants;

public class VdmPpApplicationLaunchShortcut extends VdmLaunchShortcut
{

	/**
	 * Returns a title for a type selection dialog used to prompt the user when there is more than one type that can be
	 * launched.
	 * 
	 * @return type selection dialog title
	 */
	@Override
	protected String getTypeSelectionTitle()
	{
		return "Select VDM-PP Application";
	}

	/**
	 * Returns an error message to use when the editor does not contain a type that can be launched.
	 * 
	 * @return error message when editor cannot be launched
	 */
	@Override
	protected String getEditorEmptyMessage()
	{
		return "Editor does not contain a main type";
	}

	/**
	 * Returns an error message to use when the selection does not contain a type that can be launched.
	 * 
	 * @return error message when selection cannot be launched
	 */
	@Override
	protected String getSelectionEmptyMessage()
	{
		return "Selection does not contain a launchable operation or function type";
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut#createConfiguration(org.eclipse.jdt
	 * .core.IType)
	 */
	protected ILaunchConfiguration createConfiguration(INode type,
			String projectName)
	{
		ILaunchConfiguration config = null;
		ILaunchConfigurationWorkingCopy wc = null;
		try
		{
			
			ILaunchConfigurationType configType = getConfigurationType();
			wc = configType.newInstance(null, getLaunchManager().generateLaunchConfigurationName(projectName+" "+AstNameUtil.getName(type)));
			wc.setAttribute(IDebugConstants.VDM_LAUNCH_CONFIG_PROJECT, projectName);
			wc.setAttribute(IDebugConstants.VDM_LAUNCH_CONFIG_CREATE_COVERAGE, true);

			wc.setAttribute(IDebugConstants.VDM_LAUNCH_CONFIG_DEFAULT, getModuleName( type));
			wc.setAttribute(IDebugConstants.VDM_LAUNCH_CONFIG_OPERATION, getOperationName( type)
					+ "()");
			if (isStaticAccessRequired(type))
			{
				wc.setAttribute(IDebugConstants.VDM_LAUNCH_CONFIG_MODULE, getModuleName(type));
				wc.setAttribute(IDebugConstants.VDM_LAUNCH_CONFIG_EXPRESSION, getModuleName(type)
						+ "`" + getOperationName(type) + "()");
			} else
			{
				wc.setAttribute(IDebugConstants.VDM_LAUNCH_CONFIG_MODULE, getModuleNameLaunch(type));
				wc.setAttribute(IDebugConstants.VDM_LAUNCH_CONFIG_EXPRESSION, "new "
						+ getModuleNameLaunch(type)
						+ "."
						+ getOperationName(type) + "()");
			}
			wc.setAttribute(IDebugConstants.VDM_LAUNCH_CONFIG_STATIC_OPERATION, isStaticAccessRequired(type));

			wc.setAttribute(IDebugConstants.VDM_LAUNCH_CONFIG_ENABLE_LOGGING, false);

			config = wc.doSave();
		} catch (CoreException exception)
		{

			MessageDialog.openError(VdmDebugPlugin.getActiveWorkbenchShell(), LauncherMessages.VdmLaunchShortcut_3, exception.getStatus().getMessage());
		}
		return config;
	}

	@Override
	protected INode[] filterTypes(Object[] elements, IRunnableContext context)
	{
		return new MethodSearchEngine().searchMainMethods(context, elements, MethodSearchEngine.EXPLICIT_FUNCTION
				| MethodSearchEngine.EXPLICIT_OPERATION
				| MethodSearchEngine.PUBLIC);

	}

	@Override
	protected ILaunchConfigurationType getConfigurationType()
	{
		return getLaunchManager().getLaunchConfigurationType(IVdmPpDebugConstants.ATTR_VDM_PROGRAM);
	}

}
