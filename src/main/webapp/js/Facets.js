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
                  title: 'Fournisseur',
                  collapsible: true,
                  height: 98,
                  overflowY: 'auto',
                  items: [
                      { id: 'http://univ0', layout: 'hbox', items: [ { xtype: 'checkbox', boxLabel: 'Université 0', 
                                                                       handler: this.facetedSearch, scope: this }, 
                                                                     { xtype:'label', text: '', margin: '4 0 0 5' } ] },
                      { id: 'http://univ1', layout: 'hbox', items: [ { xtype: 'checkbox', boxLabel: 'Université 1', 
                                                                       handler: this.facetedSearch, scope: this }, 
                                                                     { xtype:'label', text: '', margin: '4 0 0 5' } ] },
                      { id: 'http://univ2', layout: 'hbox', items: [ { xtype: 'checkbox', boxLabel: 'Université 2', 
                                                                       handler: this.facetedSearch, scope: this }, 
                                                                     { xtype:'label', text: '', margin: '4 0 0 5' } ] },
                      { id: 'http://univ3', layout: 'hbox', items: [ { xtype: 'checkbox', boxLabel: 'Université 3',
                                                                       handler: this.facetedSearch, scope: this }, 
                                                                     { xtype:'label', text: '', margin: '4 0 0 5' } ] },
                      { id: 'http://univ4', layout: 'hbox', items: [ { xtype: 'checkbox', boxLabel: 'Université 4', 
                                                                       handler: this.facetedSearch, scope: this }, 
                                                                     { xtype:'label', text: '', margin: '4 0 0 5' } ] }
                  ]
               },
               { xtype: 'fieldset',
                  id: 'facet1',
                  margin: '5 10 5 5',
                  padding: '0 10 5 10',
                  title: 'Langue de la prestation',
                  collapsible: true,                  
                  overflowY: 'auto',
                  height: 80,
                  items: [
                      { id: 'fr', layout: 'hbox', items: [ { xtype: 'checkbox', boxLabel: 'Français', 
                                                             handler: this.facetedSearch, scope: this }, 
                                                           { xtype:'label', text: '', margin: '4 0 0 5' } ] },
                      { id: 'en', layout: 'hbox', items: [ { xtype: 'checkbox', boxLabel: 'Anglais', 
                                                             handler: this.facetedSearch, scope: this }, 
                                                           { xtype:'label', text: '', margin: '4 0 0 5' } ] }
                  ]
               },
               { xtype: 'fieldset',
                  id: 'hiddenfacet2',
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
                    comp.getComponent(1).setText("(" + data.count + ")");
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