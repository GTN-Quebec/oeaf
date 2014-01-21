Ext.define( 'Proeaf.LearningOpportunity', {
    extend: 'Ext.form.Panel',
    layout: 'vbox',
    initComponent: function (config) { 

        this.logo = Ext.create('Ext.Img', {
            height: 100,
            border: true,
            margin: '0 5 0 0',
            style: {
                borderColor: '#B5B8C8',
                borderStyle: 'solid',
            }
        });

        this.sigle = Ext.create('Ext.form.field.Text', {
            name: 'sigle',
            width: 150,
            readOnly: true,  
            fieldStyle: { 
                border: 'none', 
                fontWeight: 'bold',
                fontSize: '20px' 
            }            
        });
      
        this.loTitle = Ext.create('Ext.form.field.Text', {
            name: 'title',
            width: 400,
            readOnly: true,
            fieldStyle: { 
                border: 'none', 
                fontSize: '16px' 
            }
        });

        this.type = Ext.create('Ext.form.field.Text', {
            name: 'oppType',
            readOnly: true,
            margin: '0 0 0 20',
            width: 300,
            fieldLabel: 'Type',
            labelWidth: 30,
            labelClsExtra: 'detailLabel',
            fieldCls: 'detailField'
        });

        this.level = Ext.create('Ext.form.field.Text', {
            name: 'educLevel',
            readOnly: true,
            margin: '0 0 0 20',
            width: 300,
            fieldLabel: tr('Level'),
            labelWidth: 40,
            labelClsExtra: 'detailLabel',
            fieldCls: 'detailField'
        });

        this.credit = Ext.create('Ext.form.field.Text', {
            name: 'credit',
            readOnly: true,
            margin: '0 0 0 20',
            fieldLabel: tr('Credit') + 's',
            labelWidth: 40,
            labelClsExtra: 'detailLabel',
            fieldCls: 'detailField'
        });

        this.subject = Ext.create('Ext.form.field.Text', {
            name: 'subject',
            readOnly: true,
            margin: '0 0 0 20',
            width: 300,
            fieldLabel: tr('Subject'),
            labelWidth: 30,
            labelClsExtra: 'detailLabel',
            fieldCls: 'detailField'
        });

        this.descr = Ext.create('Ext.panel.Panel', {
            width: '100%',
            margin: '0 20 0 20'
        });
                
        this.prerequisite = Ext.create('Ext.panel.Panel', {
            width: '100%',
            margin: '0 20 0 20'
        });                

        this.lastMinuteInfos = Ext.create('Ext.panel.Panel', {
            layout: 'fit',
            width: 300,
            height: 100,
            margin: '0 20 0 0'
        });

        this.loGrid = Ext.create('Proeaf.ConcreteLearningOpportunityGrid', {            
            lang: this.lang,
            title: tr('Schedule'),
            cls: 'schedule',
            border: false,
            margin: '6 0 0 0',
            style: {
                borderColor: '#B5B8C8',
                borderStyle: 'solid',
                borderWidth: '1px'                
            },
            width: 450,
            height: 294
        });

        this.loGrid.on( 'itemclick', this.fetchCloDetails, this );

        this.location = Ext.create('Ext.form.FieldSet', {
            title: tr('Geographical location'),
            layout: 'fit',
            width: 500,
            minWidth: 400,
            height: 300,
            minHeight: 300,
            resizable: true,
            resizeHandles: 's e se' 
        });

        cfg = {                 
            items: [ 
               { xtype: 'tbspacer', height: 10 },
               { layout: 'hbox', 
                 items: [ this.logo,  
                          { layout: 'vbox', height: 105, 
                            items: [ { xtype: 'tbfill' }, this.sigle, this.loTitle ] }
                        ]
               },
               { xtype: 'tbspacer', height: 10 },
               { layout: 'hbox',
                 width: '100%', 
                 items: [ { layout: 'vbox', 
                            items: [ this.type, this.level, this.credit, this.subject ] },
                          { xtype: 'tbfill' },
                          this.lastMinuteInfos, { xtype: 'tbfill' }
                        ]
               },
               { xtype: 'tbspacer', height: 10 },
               this.descr,
               { xtype: 'tbspacer', height: 20 },
               this.prerequisite,
               { xtype: 'tbspacer', height: 20 },
               { layout: 'hbox', 
                 items: [ { xtype: 'tbspacer', width: 20 }, 
                          this.loGrid,
                          { xtype: 'tbspacer', width: 20 }, 
                          { layout: 'vbox', 
                            items: [ this.location ] }
                        ]
               },
               { xtype: 'tbspacer', height: 20 }
            ]                        
        };
        Ext.apply(this, cfg);
        this.callParent(arguments);          
    },
    init: function(glo) {        
        this.glo = glo;
        this.fetchGloDetails();
        this.loGrid.init(glo);
    },
    fetchGloDetails: function() {        
        Ext.Ajax.request( {
            url: 'rest/queryEngine/genericLODetails?uri=' + this.glo + '&lang=' + this.lang,
            method: 'GET',
            success: function( response ) {
                var details = Ext.JSON.decode(response.responseText, true);
                if (details.providerLogo != undefined)
                    this.logo.setSrc(details.providerLogo);
                if (details.descr != undefined)
                    this.descr.add( { html: details.descr } );
                if (details.prealable != undefined)
                    this.prerequisite.add( { html: '<B><font color="#15569B">' + tr('Prerequisite') + '</font></b>: ' + details.prealable } );
                this.getForm().setValues(details);
            },
            failure: function( response ) {
                Ext.Msg.alert( 'error' );
            },
            scope: this
        } );        
    },
    fetchCloDetails: function(grid, record) {   
        this.lastMinuteInfos.removeAll();     
        this.location.removeAll();

        Ext.Ajax.request( {
            url: 'rest/queryEngine/concreteLODetails?uri=' + record.data.uri + '&lang=' + this.lang,
            method: 'GET',
            success: function( response ) {
                var details = Ext.JSON.decode(response.responseText, true);
                if (details.lastMinInfos != undefined) 
                    this.lastMinuteInfos.add( { 
                        title: tr('Last minute infos'),
                        border: true,  
                        autoScroll: true,                                
                        html: '<div style="margin: 10px">' + details.lastMinInfos + '</div>'
                    } );
                this.setMap(details.location);
            },
            failure: function( response ) {
                Ext.Msg.alert( 'error' );
            },
            scope: this
        } );
    },
    setMap: function(location) {
        if (location == undefined) 
            return;   
        
        var lat = parseFloat(location.lat);
        var long = parseFloat(location.long);
        var map = Ext.create('Ext.ux.GMapPanel', {
            margin: '2 0 10 0',  
            mapOptions: { 
                zoom: 14,
                mapTypeId: google.maps.MapTypeId.ROADMAP 
            },
            center: {
                lat: lat,
                lng: long
            },
            markers: [{
                lat: lat,
                lng: long,
                title: replaceAll( location.description, "\\\\n", "\n" )
            }]
        });
        this.location.add(map);
    },  
    clear: function() {     
        this.getForm().reset();   
        this.descr.removeAll();
        this.prerequisite.removeAll();
        this.lastMinuteInfos.removeAll();
        this.loGrid.clear();
        this.location.removeAll();
    }     
} );


