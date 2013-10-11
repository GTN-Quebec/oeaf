function tr( str ) {
    if( typeof( i18n ) != 'undefined' && i18n[ str ] )
        return( i18n[ str ] );
    return( str );
}
