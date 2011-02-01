package org.intermine.api.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

public class NameUtilTest extends TestCase {

    String[] names = {"newList_copy1", "name2"};
    
    public void testIsValidName() {
        assertFalse(NameUtil.isValidName(""));
        assertFalse(NameUtil.isValidName(" "));
        assertTrue(NameUtil.isValidName("valid"));
        assertTrue(NameUtil.isValidName("1234567890"));
        assertTrue(NameUtil.isValidName("_"));
        assertTrue(NameUtil.isValidName("ABCDEFGHIJKLMNOQRSTUVWXYZ"));
        assertTrue(NameUtil.isValidName("Valid list name with spaces"));
        assertTrue(NameUtil.isValidName("dot.dot.dot"));
        assertTrue(NameUtil.isValidName("dash-dash-dash"));
        assertTrue(NameUtil.isValidName("colon:colon:colon"));
        
        String badChars  = "¬!£$%^&*()+}{@~?<,/;'#][";
        
        for (int i = 0; i < badChars.length(); i++) {
            char c = badChars.charAt(i);
            String badName = c + "invalid " + c;
            assertFalse(NameUtil.isValidName(badName));
        }
    }
    
    public void testGenerateNewName() {

        String listName = "newList";
        String newListName = NameUtil.generateNewName(new HashSet<String>(Arrays.asList(names)), listName);
        assertEquals("newList_copy2", newListName);
        
        newListName = NameUtil.generateNewName(new HashSet(), listName);
        assertEquals("newList_copy1", newListName);
    }
    
    public void testValidateName() {
        List listNames = new ArrayList();
        listNames.add("name");
        String name = "name";
        assertEquals("name_1", NameUtil.validateName(listNames, name));
        listNames.add("name_1");
        assertEquals("name_2", NameUtil.validateName(listNames, name));
        
        String badListName = "badListName&";
        String correctedBadListName = "badListName_AMPERSAND_";
        
        assertEquals(correctedBadListName, NameUtil.validateName(listNames, badListName));
        
        listNames.add(correctedBadListName);
        assertEquals(correctedBadListName + "_1", NameUtil.validateName(listNames, badListName));

        assertEquals("a_SPECIAL_CHARACTER_REMOVED_b", NameUtil.validateName(listNames, "a`b"));
    }
    
    public void testFindNewQueryName() {
        Set<String> listNames = new HashSet();
        listNames.add("query_1");
        assertEquals("query_2", NameUtil.findNewQueryName(listNames));
        
        listNames.add("query_2");
        assertEquals("query_3", NameUtil.findNewQueryName(listNames, "query_1"));
        assertEquals("newQuery", NameUtil.findNewQueryName(listNames, "newQuery"));
    }    
}
