var REFRESH_TIMEOUT = 5000; // 5 secs

Ext.define( 'Proeaf.Harvester', {
    extend: 'Ext.panel.Panel',
    layout: 'border',
    initComponent: function( config ) {
      
        var repoTypes = Ext.create( 'Ext.data.Store', {
            fields: [ 'id', 'name' ],
            data: [  
                { 'id': 'rdf', 'name': 'RDF/XML' }, 
                { 'id': 'html', 'name': 'HTML5 + RDFa' }, 
                { 'id': 'xhtml', 'name': 'XHTML + RDFa' }
            ]
        } );

        this.startHarvester = function() {
            var form = this.newHarvestPanel.getForm();
            if( !form.isValid() ) {
                Ext.Msg.alert( tr( 'Error' ), tr( 'Fill mandatory fields with proper values.' ) );
                return;
            }
           
            form.submit( {
                success: function( form, action ) {
                    Ext.Msg.alert( tr( 'Information' ), tr( 'The harvest was launched successfully.' ) );
                    this.refresh();
                },
                failure: function( form, action ) {
                    Ext.Msg.alert( tr( 'Error' ), tr( 'An error has occurred.  The harvest have not been launched.' ) );
                },
                scope: this
            } );
        }

        this.statusMessagePanel = Ext.create( 'Ext.panel.Panel', {
            html: tr( 'No harvesting is in progress.' ),
            height: 60,
            width: 450,
            padding: 10
        } );

        this.statusPanel = Ext.create( 'Ext.panel.Panel', {
            title: 'Status',
            layout: 'vbox',
            items: [ this.statusMessagePanel ] 
        } );
        
        this.newHarvestPanel = Ext.create( 'Ext.form.Panel', {
            title: tr( 'Harvest a repository' ),
            height: 180,
            width: 450,
            bodyPadding: 10,
            defaultType: 'textfield',
            url: 'rest/harvester',
            method: 'POST',
            items: [
                {
                    fieldLabel: tr( 'Name' ),
                    name: 'name',
                    allowBlank: false
                }, {
                    fieldLabel: tr( 'URL' ),
                    name: 'url',
                    allowBlank: false,
                    width: 400
                }, {
                    fieldLabel: tr( 'Type' ),
                    name: 'type',
                    xtype: 'combobox',
                    autoSelect: true,
                    editable: false,
                    store: repoTypes,
                    queryMode: 'local',
                    valueField: 'id',
                    displayField: 'name',
                    allowBlank: false
                }
            ],
            buttons: [ {
                text: tr( 'Launch' ),
                handler: this.startHarvester,
                scope: this
            } ]
        } );

        var cfg = {
            id: 'harvester',
            layout: 'vbox',
            items: [ this.statusPanel, this.newHarvestPanel ]
        };
        
        Ext.apply( this, cfg );
        this.callParent( arguments ); 

        setTimeout( "Ext.getCmp( 'harvester' ).refresh();", REFRESH_TIMEOUT );
    },
    refresh: function() {
        Ext.Ajax.request( {
            url: 'rest/harvester/status',
            method: 'GET',
            success: function( response ) {
                if( 'NO_HARVEST' == response.responseText ) {
                    this.statusMessagePanel.body.update( tr( 'No harvesting is in progress.' ) );
                    this.newHarvestPanel.setVisible( true );
                }
                else if( 'HARVEST_TERMINATED' == response.responseText ) {
                    this.statusMessagePanel.body.update( tr( 'Harvesting is completed.' ) );
                    this.newHarvestPanel.setVisible( true );
                }
                else if( 'HARVEST_IN_PROGRESS' == response.responseText ) {
                    this.statusMessagePanel.body.update( tr( 'Harvesting is in progress...' ) );
                    this.newHarvestPanel.setVisible( false );
                    setTimeout( "Ext.getCmp( 'harvester' ).refresh();", REFRESH_TIMEOUT );
                }
            },
            failure: function( response ) {
                Ext.Msg.alert( tr( 'Error' ), tr( "Cannot access harvester's status." ) );
            },
            scope: this
        } );
    }
} );

Ext.onReady( function() {

    var harvester = Ext.create('Proeaf.Harvester', {
        border: false,
        region: 'center'
    });

    new Ext.Viewport( {
        layout: 'fit',   
        items: {
            layout: 'border',
            border: false,
            //items: [ { html: tr( '<font size="5"><b>Harvester</b></font>' ), margin: '5 0 0 10', region: 'north' }, harvester ] 
            items: [ { html: tr( '<h1>Harvester</h1>' ), margin: '5 0 0 10', region: 'north' }, harvester ] 
        } 
    } );

} );
