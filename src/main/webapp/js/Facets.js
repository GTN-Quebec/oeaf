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
                  id: 'facet0',
                  margin: '5 10 10 5',
                  padding: '0 10 5 10',
                  title: tr('Provider'),
                  collapsible: true,
                  height: 80,
                  overflowY: 'auto'
               },
               { xtype: 'fieldset',
                  id: 'facet1',
                  margin: '5 10 5 5',
                  padding: '0 10 5 10',
                  title: tr('Performance language'),
                  collapsible: true,                  
                  height: 80,
                  overflowY: 'auto'
               },
               { xtype: 'fieldset',
                  id: 'hfacet2',
                  margin: '5 10 5 5',
                  padding: '5 10 5 5',
                  title: tr('Educational level'),
                  collapsible: true,                  
                  height: 148,
                  overflowY: 'auto'
               },
               { xtype: 'fieldset',
                  id: 'hiddenfacet3',
                  margin: '5 10 5 5',
                  title: 'Date de la prestation',
                  collapsible: true,                  
                  items: [ { layout: 'fit', margin: '5 0 10 0', items: [ { xtype: 'datefield', editable: false } ] } ]
               },
               { xtype: 'button',
                  margin: '5 10 5 5',
                  text: tr('See all opportunities'),
                  handler: this.getAll,
                  scope: this
               }
            ]
        };        

        Ext.apply(this, cfg);
        this.callParent(arguments); 

        this.createFacet0();
        this.createFacet1();
        this.createFacet2();
    },
    createFacet0: function() {
        var facet = this.getComponent(0);
        Ext.Ajax.request( {
            url: 'rest/queryEngine/providers',
            method: 'GET',
            success: function( response ) {
                var providers = Ext.JSON.decode(response.responseText, true).providers;
                for (i = 0; i < providers.length; i++) {
                    var provider = providers[i];
                    var elem = { id: provider.uri, layout: 'hbox', 
                                 items: [ { xtype: 'checkbox', boxLabel: provider.name, 
                                            handler: this.facetedSearch, scope: this },
                                          { xtype:'label', text: '', margin: '4 0 0 5' } ] };
                    facet.add(elem);
                }
            },
            failure: function( response ) {
                Ext.Msg.alert( 'error' );
            },
            scope: this
        } );
    },
    createFacet1: function() {
        var facet = this.getComponent(1);
        Ext.Ajax.request( {
            url: 'rest/queryEngine/performanceLanguages?lang=' + this.lang,
            method: 'GET',
            success: function( response ) {
                var languages = Ext.JSON.decode(response.responseText, true).performanceLanguages;
                for (i = 0; i < languages.length; i++) {
                    var language = languages[i];
                    var elem = { id: language.lang, layout: 'hbox', 
                                 items: [ { xtype: 'checkbox', boxLabel: language.name, 
                                            handler: this.facetedSearch, scope: this },
                                          { xtype:'label', text: '', margin: '4 0 0 5' } ] };
                    facet.add(elem);
                }
            },
            failure: function( response ) {
                Ext.Msg.alert( 'error' );
            },
            scope: this
        } );
    },
    createFacet2: function() {

        var conceptProxy = Ext.create('Ext.data.proxy.Ajax', {
            url: 'rest/vocs/'+ encodeURIComponent("http://normetic.org/uri/profil_oeaf/v1.0/va2.2") +'/topConcepts?lang=' + this.lang,
            reader: {
                type: 'json',
                root: 'concepts',
                concept: 'concept',
                label: 'label'
            }
        });

        var store = Ext.create('Ext.data.TreeStore', {
            model: 'VocabularyConceptModel',
            proxy: conceptProxy,
        });

        store.on( 'beforeexpand', function(node) {
           if (node.getData().uri != "")
               conceptProxy.url = 'rest/vocs/'+ encodeURIComponent(node.getData().uri) +'/children?lang=' + this.lang;
        }, this);

        var tree = Ext.create('Ext.tree.Panel', {
            margin: '-1 0 0 -5',                
            store: store,
            hideHeaders: true, 
            rootVisible: false,
            useArrows: true,  
            columns: [
                { xtype: 'treecolumn', dataIndex: 'label', flex: 1 }
            ],
            selModel: {
                //mode: 'SIMPLE'
            }
        });

        var elem = { id: 'notset', layout: 'fit', 
                         items: tree };
        
        var facet = this.getComponent(2);
        facet.add(elem);
    },
    getAll: function() {
        this.isUpdateProcess = true;

        for (i = 0; i < this.items.length; i++) {
            var facet = this.getComponent(i);
            if (!facet.getId().startsWith("facet"))
                continue;
            for (j = 0; j < facet.items.length; j++) {
                var comp = facet.getComponent(j);
                comp.getComponent(0).setValue(false);
            }
        }

        this.isUpdateProcess = false;

        this.facetedSearch();
    },
    facetedSearch: function() {
        if (this.isUpdateProcess)
            return;
        var criterias = this.getCriterias();
        searchManager.doQuery(criterias)
    },
    getCriterias: function() {
        var criterias = new Array();
        var c = 0;
        for (i = 0; i < this.items.length; i++) {
            var facet = this.getComponent(i);
            if (!facet.getId().startsWith("facet"))
                continue;
            var facetCriterias = {};
            facetCriterias.id = facet.getId();
            var values = new Array();
            var k = 0;
            for (j = 0; j < facet.items.length; j++) {
                var comp = facet.getComponent(j);
                if (comp.getComponent(0).getValue()) {
                    values[k] = {};
                    values[k].id = comp.getId();
                    k++;
                }
            }
            if (values.length > 0) {
                facetCriterias.values = values;
                criterias[c] = facetCriterias; 
                c++;
            }            
        }
        return criterias;  
    },
    updateFacets: function(facetInfos) {
        this.isUpdateProcess = true;

        for (i = 0; i < facetInfos.length; i++) {
            var infos = facetInfos[i];
            var values = infos.values;
            var facet = this.getComponent(infos.id);
            //mark all checkbox for init
            for (j = 0; j < facet.items.length; j++) {
                var comp = facet.getComponent(j);
                comp.getComponent(1).setText("x");
            }
            //manage checkbox which have facet values
            for (j = 0; j < values.length; j++) {
                var data = values[j];
                var comp = facet.getComponent(data.id);
                if (comp != undefined) {
                    comp.getComponent(0).setDisabled(false);
                    comp.getComponent(1).setText("<font color='blue'>[" + data.count + "]</font>", false);
                }
            }            
            //disable and uncheck others
            for (j = 0; j < facet.items.length; j++) {
                var comp = facet.getComponent(j);
                if (comp.getComponent(1).text == "x") {
                    comp.getComponent(0).setValue(false);
                    comp.getComponent(0).setDisabled(true);
                    comp.getComponent(1).setText("");
                }
            }
        }

        this.isUpdateProcess = false;
    }
});

Ext.define('VocabularyConceptModel', {
    extend: 'Ext.data.Model',
    fields: [ 'uri', 'label' ]
});