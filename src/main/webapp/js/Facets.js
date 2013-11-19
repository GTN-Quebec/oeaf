Ext.define( 'Proeaf.Facets', {
    extend: 'Ext.panel.Panel',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    initComponent: function( config ) {
        
        cfg = {
            collapsible: true,
            autoScroll: true,
            items: [ 
               { xtype: 'fieldset',
                  margin: '5 10 5 5',
                  title: 'Fournisseur',
                  collapsible: true,
                  height: 110,
                  autoScroll: true,
                  items: [
                      { xtype: 'checkbox', boxLabel: 'TELUQ' },
                      { xtype: 'checkbox', boxLabel: 'UQAM' },
                      { xtype: 'checkbox', boxLabel: 'Université de Montréal' }
                  ]
               },
               { xtype: 'fieldset',
                  margin: '5 10 5 5',
                  padding: '0 10 5 10',
                  title: 'Langue de la prestation',
                  collapsible: true,                  
                  autoScroll: true,
                  height: 110,
                  items: [
                      { xtype: 'checkbox', boxLabel: 'Français' },
                      { xtype: 'checkbox', boxLabel: 'Anglais' },
                      { xtype: 'checkbox', boxLabel: 'Espagnol' },
                      { xtype: 'checkbox', boxLabel: 'Allemand' },
                      { xtype: 'checkbox', boxLabel: 'Russe' },
                      { xtype: 'checkbox', boxLabel: 'Arabe' },
                      { xtype: 'checkbox', boxLabel: 'Italien' },
                      { xtype: 'checkbox', boxLabel: 'Chinois' },
                      { xtype: 'checkbox', boxLabel: 'Autre' }
                  ]
               },
               { xtype: 'fieldset',
                  margin: '5 10 5 5',
                  title: 'Date de la prestation',
                  collapsible: true,                  
                  items: [ { layout: 'fit', margin: '5 0 10 0', items: [ { xtype: 'datefield', editable: false } ] } ]
               }
            ]
        };        

        Ext.apply(this, cfg);
        this.callParent(arguments); 
    }
});