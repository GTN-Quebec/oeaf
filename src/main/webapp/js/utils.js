/***********************/
/*** Authorized flag ***/
/***********************/
var authorized = false;
function setAuthorized(callback) {
    Ext.Ajax.request( {
        url: '/rest/security/isAuthorized',
        method: 'GET',
        success: function(response, opts) {
            authorized = response.responseText == 'true';
            callback.call();
        }
    } );
}


/**************************************************/
/*** Miscellaneous utility functions or objects ***/
/**************************************************/

Ext.override(Ext.Window, {
    constrain: true
});


function utilsInit( lg ) {
    vocabProxy.url = vocabularyUrl + '/rest/vocs?lang=' + lg;
    vocCtxtProxy.url = vocabularyUrl + '/rest/vocContexts?lang=' + lg;
}

Ext.getUrlParam = function( param ) {
   var params = Ext.urlDecode( location.search.substring( 1 ) );
   return( param ? params[ param ] : params );
}

function replaceAll( str, strToLookFor, strToReplaceWith ) {
    return( str.replace( new RegExp( strToLookFor, 'g' ), strToReplaceWith ) );
}

// Strip leading and trailing whitespace characters and replace all repeated whitespace characters into 1 space (like XSLT's normalize-space() function). 
function normalizeSpace( str ) {
    return( str.replace( /^\s*|\s(?=\s)|\s*$/g, '' ) );
}
Ext.Ajax.timeout = 120000; // Overwrite default Ajax timeout to 2 minutes.


function normalizeSearchQuery( query ) {
    // Handle double quotes here and other special keywords or criterias.
    // TODO

    // Remove undesirable commas.
    var newValue = replaceAll( query, ',', ' ' );

    // Normalize whitespace characters.
    newValue = normalizeSpace( newValue );

    return( newValue );
}

//only FF has startsWith and endsWith methods !
//patch for IE, Safari, Chrome, Opera -AM

if (!String.prototype.startsWith) {
     String.prototype.startsWith = function (str) {
         return this.slice(0, str.length) == str;
     };
}
 
if (!String.prototype.endsWith) {
     String.prototype.endsWith = function(str) {
         return this.slice(-str.length) == str;
     };
}

if (!Array.prototype.indexOf) {
     Array.prototype.indexOf = function(obj, start) {
         for (var i = (start || 0), j = this.length; i < j; i++) {
             if (this[i] === obj) { return i; }
         }
         return -1;
     }
}

function getlanguage( lang ) {
    if (lang == "fra")
        return "French";
    else if (lang == "eng")
        return "English";
 
    return null;   
}


/**
  * format ISO date
  */
function formatISODate( value, lang ) {
    var date = new Date(value);
    if (lang == 'fr')
        return Ext.Date.format(date, 'j F Y à H:i');
    else 
        return Ext.Date.format(date, 'F j, Y, g:i a');
};


/**
  * format ISO duration in h:m
  * ex: P185M -> 3h05
  */
function formatISODuration( duration ) {
    var seconds = 0;
    duration = duration.substring(1); //ignore P
    var i = duration.indexOf('H')
    if (i != -1) {
        var hour = parseInt(duration.substring(0, i));
        seconds = hour * 3600;
        duration = duration.substring(i + 1); 
    }
    i = duration.indexOf('M')
    if (i != -1) {
        var minute = parseInt(duration.substring(0, i));
        seconds = minute * 60;
    }
    var res = '';
    var h = seconds / 3600;
    seconds = seconds % 3600;
    var m = seconds / 60;
    if (m == 0)
        m = '';
    else if (m < 10)
        m = '0' + m;
    return h + 'h' + m;    
}
