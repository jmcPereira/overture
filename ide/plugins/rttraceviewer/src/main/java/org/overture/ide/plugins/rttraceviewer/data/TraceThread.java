package org.overture.ide.plugins.rttraceviewer.data;

import java.util.Stack;

public class TraceThread extends TraceResource
{
    private Long id;
    private Boolean blocked;
    private Stack<TraceObject> objectStack;

    public TraceThread(Long id)
    {
    	objectStack = new Stack<TraceObject>();

    	this.id = id;
    	
    	blocked = new Boolean(false);   	
    }

    public Long getId()
    {
        return id;
    }
    
    public void setStatus(Boolean pblocked)
    {
        blocked = pblocked;
    }

    public Boolean getStatus()
    {
        return blocked;
    }
      
    public void pushCurrentObject(TraceObject currentObjectId)
    {
    	objectStack.push(currentObjectId);
    }
    
    public void popCurrentObject()
    {
    	objectStack.pop();
    }
    
    public TraceObject getCurrentObject()
    {	
    	return objectStack.peek();
    }
    
    public boolean hasCurrentObject()
    {
    	
    	return !objectStack.isEmpty();
    }

}