package org.intermine.objectstore.intermine;

/*
 * Copyright (C) 2002-2004 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;

import org.intermine.testing.OneTimeTestCase;
import org.intermine.sql.DatabaseFactory;
import org.intermine.util.TypeUtil;

public class TruncatedSqlGeneratorTest extends SqlGeneratorTest
{
    public TruncatedSqlGeneratorTest(String arg) {
        super(arg);
    }

    public static Test suite() {
        return OneTimeTestCase.buildSuite(TruncatedSqlGeneratorTest.class);
    }

    public static void oneTimeSetUp() throws Exception {
        SqlGeneratorTest.oneTimeSetUp();
        setUpResults();
        db = DatabaseFactory.getDatabase("db.unittest");
    }

    public static void setUpResults() throws Exception {
        results.put("SelectSimpleObject", "SELECT intermine_Alias.OBJECT AS \"intermine_Alias\", intermine_Alias.id AS \"intermine_Aliasid\" FROM InterMineObject AS intermine_Alias WHERE intermine_Alias.class = 'org.intermine.model.testmodel.Company' ORDER BY intermine_Alias.id");
        results.put("SubQuery", "SELECT DISTINCT intermine_All.intermine_Arrayname AS a1_, intermine_All.intermine_Alias AS \"intermine_Alias\" FROM (SELECT DISTINCT intermine_Array.OBJECT AS intermine_Array, intermine_Array.CEOId AS intermine_ArrayCEOId, intermine_Array.addressId AS intermine_ArrayaddressId, intermine_Array.id AS intermine_Arrayid, intermine_Array.name AS intermine_Arrayname, intermine_Array.vatNumber AS intermine_ArrayvatNumber, 5 AS intermine_Alias FROM InterMineObject AS intermine_Array WHERE intermine_Array.class = 'org.intermine.model.testmodel.Company') AS intermine_All ORDER BY intermine_All.intermine_Arrayname, intermine_All.intermine_Alias");
        results.put("WhereSimpleEquals", "SELECT DISTINCT a1_.name AS a2_ FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a1_.vatNumber = 1234 ORDER BY a1_.name");
        results.put("WhereSimpleNotEquals", "SELECT DISTINCT a1_.name AS a2_ FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a1_.vatNumber != 1234 ORDER BY a1_.name");
        results.put("WhereSimpleNegEquals", "SELECT DISTINCT a1_.name AS a2_ FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a1_.vatNumber != 1234 ORDER BY a1_.name");
        results.put("WhereSimpleLike", "SELECT DISTINCT a1_.name AS a2_ FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a1_.name LIKE 'Company%' ORDER BY a1_.name");
        results.put("WhereEqualsString", "SELECT DISTINCT a1_.name AS a2_ FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a1_.name = 'CompanyA' ORDER BY a1_.name");
        results.put("WhereAndSet", "SELECT DISTINCT a1_.name AS a2_ FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND (a1_.name LIKE 'Company%' AND a1_.vatNumber > 2000) ORDER BY a1_.name");
        results.put("WhereOrSet", "SELECT DISTINCT a1_.name AS a2_ FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND (a1_.name LIKE 'CompanyA%' OR a1_.vatNumber > 2000) ORDER BY a1_.name");
        results.put("WhereNotSet", "SELECT DISTINCT a1_.name AS a2_ FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND ( NOT (a1_.name LIKE 'Company%' AND a1_.vatNumber > 2000)) ORDER BY a1_.name");
        results.put("WhereSubQueryField", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a1_.name AS orderbyfield0 FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Department' AND a1_.name IN (SELECT DISTINCT a1_.name FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Department') ORDER BY a1_.name, a1_.id");
        results.put("WhereSubQueryClass", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a1_.id IN (SELECT DISTINCT a1_.id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a1_.name = 'CompanyA') ORDER BY a1_.id");
        results.put("WhereNotSubQueryClass", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a1_.id NOT IN (SELECT DISTINCT a1_.id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a1_.name = 'CompanyA') ORDER BY a1_.id");
        results.put("WhereNegSubQueryClass", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a1_.id NOT IN (SELECT DISTINCT a1_.id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a1_.name = 'CompanyA') ORDER BY a1_.id");
        results.put("WhereClassClass", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM InterMineObject AS a1_, InterMineObject AS a2_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a2_.class = 'org.intermine.model.testmodel.Company' AND a1_.id = a2_.id ORDER BY a1_.id, a2_.id");
        results.put("WhereNotClassClass", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM InterMineObject AS a1_, InterMineObject AS a2_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a2_.class = 'org.intermine.model.testmodel.Company' AND a1_.id != a2_.id ORDER BY a1_.id, a2_.id");
        results.put("WhereNegClassClass", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM InterMineObject AS a1_, InterMineObject AS a2_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a2_.class = 'org.intermine.model.testmodel.Company' AND a1_.id != a2_.id ORDER BY a1_.id, a2_.id");
        Integer id1 = (Integer) TypeUtil.getFieldValue(data.get("CompanyA"), "id");
        results.put("WhereClassObject", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a1_.id = " + id1 + " ORDER BY a1_.id");
        results.put("Contains11", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM InterMineObject AS a1_, InterMineObject AS a2_ WHERE a1_.class = 'org.intermine.model.testmodel.Department' AND a2_.class = 'org.intermine.model.testmodel.Manager' AND (a1_.managerId = a2_.id AND a1_.name = 'DepartmentA1') ORDER BY a1_.id, a2_.id");
        results.put("ContainsNot11", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM InterMineObject AS a1_, InterMineObject AS a2_ WHERE a1_.class = 'org.intermine.model.testmodel.Department' AND a2_.class = 'org.intermine.model.testmodel.Manager' AND (a1_.managerId != a2_.id AND a1_.name = 'DepartmentA1') ORDER BY a1_.id, a2_.id");
        results.put("ContainsNeg11", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM InterMineObject AS a1_, InterMineObject AS a2_ WHERE a1_.class = 'org.intermine.model.testmodel.Department' AND a2_.class = 'org.intermine.model.testmodel.Manager' AND (a1_.managerId != a2_.id AND a1_.name = 'DepartmentA1') ORDER BY a1_.id, a2_.id");
        results.put("Contains1N", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM InterMineObject AS a1_, InterMineObject AS a2_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a2_.class = 'org.intermine.model.testmodel.Department' AND (a1_.id = a2_.companyId AND a1_.name = 'CompanyA') ORDER BY a1_.id, a2_.id");
        results.put("ContainsN1", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM InterMineObject AS a1_, InterMineObject AS a2_ WHERE a1_.class = 'org.intermine.model.testmodel.Department' AND a2_.class = 'org.intermine.model.testmodel.Company' AND (a1_.companyId = a2_.id AND a2_.name = 'CompanyA') ORDER BY a1_.id, a2_.id");
        results.put("ContainsMN", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM InterMineObject AS a1_, InterMineObject AS a2_, CompanysContractors AS indirect0 WHERE a1_.class = 'org.intermine.model.testmodel.Contractor' AND a2_.class = 'org.intermine.model.testmodel.Company' AND ((a1_.id = indirect0.Companys AND indirect0.Contractors = a2_.id) AND a1_.name = 'ContractorA') ORDER BY a1_.id, a2_.id");
        results.put("ContainsDuplicatesMN", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM InterMineObject AS a1_, InterMineObject AS a2_, OldComsOldContracts AS indirect0 WHERE a1_.class = 'org.intermine.model.testmodel.Contractor' AND a2_.class = 'org.intermine.model.testmodel.Company' AND (a1_.id = indirect0.OldComs AND indirect0.OldContracts = a2_.id) ORDER BY a1_.id, a2_.id");
        id1 = (Integer) TypeUtil.getFieldValue(data.get("EmployeeA1"), "id");
        results.put("ContainsObject", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Department' AND a1_.managerId = " + id1 + " ORDER BY a1_.id");
        results.put("SimpleGroupBy", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, COUNT(*) AS a2_ FROM InterMineObject AS a1_, InterMineObject AS a3_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a3_.class = 'org.intermine.model.testmodel.Department' AND a1_.id = a3_.companyId GROUP BY a1_.OBJECT, a1_.CEOId, a1_.addressId, a1_.id, a1_.name, a1_.vatNumber ORDER BY a1_.id, COUNT(*)");
        results.put("MultiJoin", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id, a4_.OBJECT AS a4_, a4_.id AS a4_id FROM InterMineObject AS a1_, InterMineObject AS a2_, InterMineObject AS a3_, InterMineObject AS a4_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a2_.class = 'org.intermine.model.testmodel.Department' AND a3_.class = 'org.intermine.model.testmodel.Manager' AND a4_.class = 'org.intermine.model.testmodel.Address' AND (a1_.id = a2_.companyId AND a2_.managerId = a3_.id AND a3_.addressId = a4_.id AND a3_.name = 'EmployeeA1') ORDER BY a1_.id, a2_.id, a3_.id, a4_.id");
        results.put("SelectComplex", "SELECT DISTINCT (AVG(a1_.vatNumber) + 20) AS a3_, a2_.name AS a4_, a2_.OBJECT AS a2_, a2_.id AS a2_id FROM InterMineObject AS a1_, InterMineObject AS a2_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a2_.class = 'org.intermine.model.testmodel.Department' GROUP BY a2_.OBJECT, a2_.companyId, a2_.id, a2_.managerId, a2_.name ORDER BY (AVG(a1_.vatNumber) + 20), a2_.name, a2_.id");
        results.put("SelectClassAndSubClasses", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a1_.name AS orderbyfield0 FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Employee' ORDER BY a1_.name, a1_.id");
        results.put("SelectInterfaceAndSubClasses", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Employable' ORDER BY a1_.id");
        results.put("SelectInterfaceAndSubClasses2", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.RandomInterface' ORDER BY a1_.id");
        results.put("SelectInterfaceAndSubClasses3", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.ImportantPerson' ORDER BY a1_.id");
        results.put("OrderByAnomaly", "SELECT DISTINCT 5 AS a2_, a1_.name AS a3_ FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' ORDER BY a1_.name");
        Integer id2 = (Integer) TypeUtil.getFieldValue(data.get("CompanyA"), "id");
        Integer id3 = (Integer) TypeUtil.getFieldValue(data.get("DepartmentA1"), "id");
        results.put("SelectClassObjectSubquery", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_, InterMineObject AS a2_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a2_.class = 'org.intermine.model.testmodel.Department' AND (a1_.id = " + id2 + " AND a1_.id = a2_.companyId AND a2_.id IN (SELECT DISTINCT a1_.id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Department' AND a1_.id = " + id3 + ")) ORDER BY a1_.id");
        results.put("SelectUnidirectionalCollection", "SELECT DISTINCT a2_.OBJECT AS a2_, a2_.id AS a2_id FROM InterMineObject AS a1_, InterMineObject AS a2_, HasSecretarysSecretarys AS indirect0 WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a2_.class = 'org.intermine.model.testmodel.Secretary' AND (a1_.name = 'CompanyA' AND (a1_.id = indirect0.Secretarys AND indirect0.HasSecretarys = a2_.id)) ORDER BY a2_.id");
        results.put("EmptyAndConstraintSet", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND true ORDER BY a1_.id");
        results.put("EmptyOrConstraintSet", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND false ORDER BY a1_.id");
        results.put("EmptyNandConstraintSet", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND false ORDER BY a1_.id");
        results.put("EmptyNorConstraintSet", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND true ORDER BY a1_.id");
        results.put("BagConstraint", "SELECT DISTINCT Company.OBJECT AS \"Company\", Company.id AS \"Companyid\" FROM InterMineObject AS Company WHERE Company.class = 'org.intermine.model.testmodel.Company' AND (Company.name = 'CompanyA' OR Company.name = 'goodbye' OR Company.name = 'hello') ORDER BY Company.id");
        results.put("BagConstraint2", "SELECT DISTINCT Company.OBJECT AS \"Company\", Company.id AS \"Companyid\" FROM InterMineObject AS Company WHERE Company.class = 'org.intermine.model.testmodel.Company' AND (Company.id = " + id2 + ") ORDER BY Company.id");
        results.put("InterfaceField", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Employable' AND a1_.name = 'EmployeeA1' ORDER BY a1_.id");
        results.put("InterfaceReference", NO_RESULT);
        results.put("InterfaceCollection", NO_RESULT);
        Set res = new HashSet();
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a1__1.debt AS a2_, a1_.vatNumber AS a3_ FROM InterMineObject AS a1_, InterMineObject AS a1__1 WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a1_.id = a1__1.id AND a1__1.class = 'org.intermine.model.testmodel.Broke' AND (a1__1.debt > 0 AND a1_.vatNumber > 0) ORDER BY a1_.id, a1__1.debt, a1_.vatNumber");
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a1_.debt AS a2_, a1__1.vatNumber AS a3_ FROM InterMineObject AS a1_, InterMineObject AS a1__1 WHERE a1_.class = 'org.intermine.model.testmodel.Broke' AND a1_.id = a1__1.id AND a1__1.class = 'org.intermine.model.testmodel.Company' AND (a1_.debt > 0 AND a1__1.vatNumber > 0) ORDER BY a1_.id, a1_.debt, a1__1.vatNumber");
        results.put("DynamicInterfacesAttribute", res);
        res = new HashSet();
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_, InterMineObject AS a1__1 WHERE a1_.class = 'org.intermine.model.testmodel.Employable' AND a1_.id = a1__1.id AND a1__1.class = 'org.intermine.model.testmodel.Broke' ORDER BY a1_.id");
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_, InterMineObject AS a1__1 WHERE a1_.class = 'org.intermine.model.testmodel.Broke' AND a1_.id = a1__1.id AND a1__1.class = 'org.intermine.model.testmodel.Employable' ORDER BY a1_.id");
        results.put("DynamicClassInterface", res);
        res = new HashSet();
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM InterMineObject AS a1_, InterMineObject AS a1__1, InterMineObject AS a2_, InterMineObject AS a3_ WHERE a1_.class = 'org.intermine.model.testmodel.Department' AND a1_.id = a1__1.id AND a1__1.class = 'org.intermine.model.testmodel.Broke' AND a2_.class = 'org.intermine.model.testmodel.Company' AND a3_.class = 'org.intermine.model.testmodel.Bank' AND (a2_.id = a1_.companyId AND a3_.id = a1__1.bankId) ORDER BY a1_.id, a2_.id, a3_.id");
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM InterMineObject AS a1_, InterMineObject AS a1__1, InterMineObject AS a2_, InterMineObject AS a3_ WHERE a1_.class = 'org.intermine.model.testmodel.Broke' AND a1_.id = a1__1.id AND a1__1.class = 'org.intermine.model.testmodel.Department' AND a2_.class = 'org.intermine.model.testmodel.Company' AND a3_.class = 'org.intermine.model.testmodel.Bank' AND (a2_.id = a1__1.companyId AND a3_.id = a1_.bankId) ORDER BY a1_.id, a2_.id, a3_.id");
        results.put("DynamicClassRef1", res);
        res = new HashSet();
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM InterMineObject AS a1_, InterMineObject AS a1__1, InterMineObject AS a2_, InterMineObject AS a3_ WHERE a1_.class = 'org.intermine.model.testmodel.Department' AND a1_.id = a1__1.id AND a1__1.class = 'org.intermine.model.testmodel.Broke' AND a2_.class = 'org.intermine.model.testmodel.Company' AND a3_.class = 'org.intermine.model.testmodel.Bank' AND (a1_.companyId = a2_.id AND a1__1.bankId = a3_.id) ORDER BY a1_.id, a2_.id, a3_.id");
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM InterMineObject AS a1_, InterMineObject AS a1__1, InterMineObject AS a2_, InterMineObject AS a3_ WHERE a1_.class = 'org.intermine.model.testmodel.Broke' AND a1_.id = a1__1.id AND a1__1.class = 'org.intermine.model.testmodel.Department' AND a2_.class = 'org.intermine.model.testmodel.Company' AND a3_.class = 'org.intermine.model.testmodel.Bank' AND (a1__1.companyId = a2_.id AND a1_.bankId = a3_.id) ORDER BY a1_.id, a2_.id, a3_.id");
        results.put("DynamicClassRef2", res);
        res = new HashSet();
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM InterMineObject AS a1_, InterMineObject AS a1__1, InterMineObject AS a2_, InterMineObject AS a3_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a1_.id = a1__1.id AND a1__1.class = 'org.intermine.model.testmodel.Bank' AND a2_.class = 'org.intermine.model.testmodel.Department' AND a3_.class = 'org.intermine.model.testmodel.Broke' AND (a1_.id = a2_.companyId AND a1_.id = a3_.bankId) ORDER BY a1_.id, a2_.id, a3_.id");
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM InterMineObject AS a1_, InterMineObject AS a1__1, InterMineObject AS a2_, InterMineObject AS a3_ WHERE a1_.class = 'org.intermine.model.testmodel.Bank' AND a1_.id = a1__1.id AND a1__1.class = 'org.intermine.model.testmodel.Company' AND a2_.class = 'org.intermine.model.testmodel.Department' AND a3_.class = 'org.intermine.model.testmodel.Broke' AND (a1_.id = a2_.companyId AND a1_.id = a3_.bankId) ORDER BY a1_.id, a2_.id, a3_.id");
        results.put("DynamicClassRef3", res);
        res = new HashSet();
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM InterMineObject AS a1_, InterMineObject AS a1__1, InterMineObject AS a2_, InterMineObject AS a3_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND a1_.id = a1__1.id AND a1__1.class = 'org.intermine.model.testmodel.Bank' AND a2_.class = 'org.intermine.model.testmodel.Department' AND a3_.class = 'org.intermine.model.testmodel.Broke' AND (a2_.companyId = a1_.id AND a3_.bankId = a1_.id) ORDER BY a1_.id, a2_.id, a3_.id");
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a2_.OBJECT AS a2_, a2_.id AS a2_id, a3_.OBJECT AS a3_, a3_.id AS a3_id FROM InterMineObject AS a1_, InterMineObject AS a1__1, InterMineObject AS a2_, InterMineObject AS a3_ WHERE a1_.class = 'org.intermine.model.testmodel.Bank' AND a1_.id = a1__1.id AND a1__1.class = 'org.intermine.model.testmodel.Company' AND a2_.class = 'org.intermine.model.testmodel.Department' AND a3_.class = 'org.intermine.model.testmodel.Broke' AND (a2_.companyId = a1_.id AND a3_.bankId = a1_.id) ORDER BY a1_.id, a2_.id, a3_.id");
        results.put("DynamicClassRef4", res);
        res = new HashSet();
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_, InterMineObject AS a1__1, InterMineObject AS a2_, InterMineObject AS a2__1 WHERE a1_.class = 'org.intermine.model.testmodel.Employable' AND a1_.id = a1__1.id AND a1__1.class = 'org.intermine.model.testmodel.Broke' AND a2_.class = 'org.intermine.model.testmodel.HasAddress' AND a2_.id = a2__1.id AND a2__1.class = 'org.intermine.model.testmodel.Broke' AND a1_.id = a2_.id ORDER BY a1_.id");
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_, InterMineObject AS a1__1, InterMineObject AS a2_, InterMineObject AS a2__1 WHERE a1_.class = 'org.intermine.model.testmodel.Employable' AND a1_.id = a1__1.id AND a1__1.class = 'org.intermine.model.testmodel.Broke' AND a2_.class = 'org.intermine.model.testmodel.Broke' AND a2_.id = a2__1.id AND a2__1.class = 'org.intermine.model.testmodel.HasAddress' AND a1_.id = a2_.id ORDER BY a1_.id");
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_, InterMineObject AS a1__1, InterMineObject AS a2_, InterMineObject AS a2__1 WHERE a1_.class = 'org.intermine.model.testmodel.Broke' AND a1_.id = a1__1.id AND a1__1.class = 'org.intermine.model.testmodel.Employable' AND a2_.class = 'org.intermine.model.testmodel.HasAddress' AND a2_.id = a2__1.id AND a2__1.class = 'org.intermine.model.testmodel.Broke' AND a1_.id = a2_.id ORDER BY a1_.id");
        res.add("SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_, InterMineObject AS a1__1, InterMineObject AS a2_, InterMineObject AS a2__1 WHERE a1_.class = 'org.intermine.model.testmodel.Broke' AND a1_.id = a1__1.id AND a1__1.class = 'org.intermine.model.testmodel.Employable' AND a2_.class = 'org.intermine.model.testmodel.Broke' AND a2_.id = a2__1.id AND a2__1.class = 'org.intermine.model.testmodel.HasAddress' AND a1_.id = a2_.id ORDER BY a1_.id");
        results.put("DynamicClassConstraint", res);
        results.put("ContainsConstraintNull", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Employee' AND a1_.addressId IS NULL ORDER BY a1_.id");
        results.put("ContainsConstraintNotNull", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Employee' AND a1_.addressId IS NOT NULL ORDER BY a1_.id");
        results.put("SimpleConstraintNull", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Manager' AND a1_.title IS NULL ORDER BY a1_.id");
        results.put("SimpleConstraintNotNull", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Manager' AND a1_.title IS NOT NULL ORDER BY a1_.id");
        results.put("TypeCast", "SELECT DISTINCT (a1_.age)::TEXT AS a2_ FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Employee' ORDER BY (a1_.age)::TEXT");
        results.put("IndexOf", "SELECT STRPOS(a1_.name, 'oy') AS a2_ FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Employee' ORDER BY STRPOS(a1_.name, 'oy')");
        results.put("Substring", "SELECT SUBSTR(a1_.name, 2, 2) AS a2_ FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Employee' ORDER BY SUBSTR(a1_.name, 2, 2)");
        results.put("Substring2", "SELECT SUBSTR(a1_.name, 2) AS a2_ FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Employee' ORDER BY SUBSTR(a1_.name, 2)");
        results.put("OrderByReference", "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id, a1_.departmentId AS orderbyfield0 FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Employee' ORDER BY a1_.departmentId, a1_.id");
    }

    protected DatabaseSchema getSchema() {
        return new DatabaseSchema(model, Collections.singletonList(model.getClassDescriptorByName("org.intermine.model.InterMineObject")));
    }
    public String getRegisterOffset1() {
        return "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' ORDER BY a1_.id";
    }
    public String getRegisterOffset2() {
        return "SELECT DISTINCT a1_.OBJECT AS a1_, a1_.id AS a1_id FROM InterMineObject AS a1_ WHERE a1_.class = 'org.intermine.model.testmodel.Company' AND ";
    }
}
