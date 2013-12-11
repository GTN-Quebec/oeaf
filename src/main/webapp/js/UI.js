Ext.onReady( function() {

    searchManager = Ext.create('Proeaf.SearchManager', {
        border: false,
        title: tr('Search')
    });

    var harvester = Ext.create('Proeaf.Harvester', {
        border: false,
        title: tr('Harvester')
    });


    new Ext.Viewport( {
        layout: 'border',   
        items: [ 
            { region: 'north',
              margin: '5 0 10 10',               
              html: '<font size="5"><b>Prototype de mise en œuvre du profil OÉAF</b></font> (version 0.1)' },
            { xtype: 'tabpanel',            
              region: 'center',
              plain: true,
              items: [ searchManager, harvester ] }
        ]
    } );

} );
