/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mucommander.ui.action.impl;

import com.mucommander.ui.dnd.ClipboardOperations;
import com.mucommander.ui.dnd.ClipboardSupport;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;



/**
 *
 * @author Kezides
 */
public class PasteClipboardFilesActionNGTest {
    
    public PasteClipboardFilesActionNGTest() {
    }
    

    @BeforeClass
    public static void setUpClass() throws Exception {
        

    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        
        
        
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
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
