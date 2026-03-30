package com.mucommander.ui.action.impl;

import com.mucommander.ui.dnd.ClipboardOperations;
import com.mucommander.ui.dnd.ClipboardSupport;
import org.junit.jupiter.api.Test;


/**
 *
 * @author Kezides
 */
public class PasteClipboardFilesActionTest {
    /**
     * Test of performAction method, of class PasteClipboardFilesAction.
     */
    @Test
    public void testPerformAction(){
        //test paste copy operation.
        ClipboardSupport.setOperation(ClipboardOperations.COPY);
        
        assert ClipboardSupport.getOperation() == ClipboardOperations.COPY;
        
        //test paste cut operation.
        ClipboardSupport.setOperation(ClipboardOperations.CUT);
        
        assert ClipboardSupport.getOperation() == ClipboardOperations.CUT;
        
        //test paste archive operation.
        ClipboardSupport.setOperation(ClipboardOperations.ARCHIVE);
        
        assert ClipboardSupport.getOperation() == ClipboardOperations.ARCHIVE;
    }

    
}
