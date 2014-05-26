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
package org.overture.ide.debug.internal.ui.viewers.update;

import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.update.DefaultModelProxyFactory;
import org.eclipse.debug.ui.IDebugUIConstants;

@SuppressWarnings("restriction")
public class VdmModelProxyFactory extends DefaultModelProxyFactory
{
	@Override
	public IModelProxy createModelProxy(Object element,
			IPresentationContext context)
	{
		String id = context.getId();
		if (IDebugUIConstants.ID_DEBUG_VIEW.equals(id))
		{
			if (element instanceof IDebugTarget)
			{
				return new VdmDebugTargetProxy((IDebugTarget) element);
			}
		}
		return super.createModelProxy(element, context);
	}
}
