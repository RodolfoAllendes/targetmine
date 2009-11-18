<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="im" %>

<!-- chromosomeLocDisplayer.jsp -->

<html:xhtml/>
<im:instanceof instanceofObject="${interMineObject}" instanceofClass="org.intermine.model.bio.LocatedSequenceFeature" instanceofVariable="hasChromosomeLocation"/>
<c:choose>
  <c:when test="${hasChromosomeLocation == 'true' && !empty interMineObject.chromosomeLocation && !empty interMineObject.chromosomeLocation.object}">
    ${interMineObject.chromosomeLocation.object.primaryIdentifier}<c:if test="${!empty interMineObject.chromosomeLocation && !empty interMineObject.chromosomeLocation.start}">: ${interMineObject.chromosomeLocation.start}-${interMineObject.chromosomeLocation.end}
    </c:if>
  </c:when>
  <c:otherwise>
    [unknown]
  </c:otherwise>
</c:choose>

<!-- /chromosomeLocDisplayer.jsp -->
