<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%
    String protocol = request.getScheme();
    String server = request.getServerName();
    int port = request.getServerPort();
    String webapp = request.getContextPath();
    String portalUrl = protocol + "://" + server + ( port == 80 ? "" : ":" + port ) + webapp;
%>
<!doctype html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <!-- link rel="stylesheet" type="text/css" href="ext-4.2.1.883/resources/css/ext-all.css" / -->
    <link rel="stylesheet" href="ext-4.2.1.883/resources/css/ext-all-neptune.css"/>
    <link rel="stylesheet" href="ext-4.2.1.883/resources/css/example.css" />
    <link rel="stylesheet" type="text/css" href="default.css">

    <script type="text/javascript" src="ext-4.2.1.883/ext-all.js"></script>
    <script type="text/javascript" src="ext-4.2.1.883/locale/ext-lang-en.js"></script>
    <script type="text/javascript" src="ext-4.2.1.883/ext-theme-neptune.js"></script>
    <script type="text/javascript" src="ext-4.2.1.883/examples.js"></script>
    <script type="text/javascript" src="ext-4.2.1.883/GMapPanel.js"></script>
    <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?v=3&sensor=false"></script>

    <script type="text/javascript">
        var portalWebappUrl = '<%= portalUrl %>';
    </script>
    <script type="text/javascript" src="js/i18n.js"></script>
    <% if( request.getParameter( "lang" ) != null && !"en".equals( request.getParameter( "lang" ) ) ) { %>
        <script type="text/javascript" src="js/i18n_<%= request.getParameter( "lang" ) %>.js"></script>
        <script type="text/javascript" src="ext-4.2.1.883/locale/ext-lang-<%= request.getParameter( "lang" ) %>.js"></script>
    <% } %>
    <title>Prototype OÉAF</title>
    <script type="text/javascript" src="js/utils.js"></script>
    <script type="text/javascript" src="js/Facets.js"></script>
    <script type="text/javascript" src="js/LearningOpportunityGrid.js"></script>
    <script type="text/javascript" src="js/LearningOpportunity.js"></script>
    <script type="text/javascript" src="js/Search.js"></script>
    <script type="text/javascript" src="js/Harvester.js"></script>
    <script type="text/javascript" src="js/UI.js"></script>
</head>
<body>
</body>
</html>
