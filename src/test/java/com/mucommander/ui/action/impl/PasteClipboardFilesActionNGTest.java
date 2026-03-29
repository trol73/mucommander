/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mucommander.ui.action.impl;

import com.mucommander.ui.dnd.ClipboardOperations;
import com.mucommander.ui.dnd.ClipboardSupport;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


/**
 *
 * @author Kezides
 */
public class PasteClipboardFilesActionNGTest {
    
    public PasteClipboardFilesActionNGTest() {
    }
    

    @BeforeAll
    public static void setUpClass() {
        

    }

    @AfterAll
    public static void tearDownClass() {
    }


    /**
     * Test of performAction method, of class PasteClipboardFilesAction.
     */
    @Test
    public void testPerformAction(){
        System.out.println("performAction");
               
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
