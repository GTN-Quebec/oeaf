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

Array.prototype.indexOf = function(obj, start) {
     for (var i = (start || 0), j = this.length; i < j; i++) {
         if (this[i] === obj) { return i; }
     }
     return -1;
}



