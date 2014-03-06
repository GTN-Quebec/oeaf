Ext.define( 'Proeaf.Facets', {
    extend: 'Ext.panel.Panel',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    initComponent: function( config ) {

        this.clearButton = new Ext.button.Button({
            text: tr('Clear'), 
            handler:this.clearCriterias, 
            scope: this, 
            disabled: true 
        });

        cfg = {
            autoScroll: true,
            tools: [ this.clearButton ],
            items: [ 
               { xtype: 'fieldset',
                  id: 'facet0',
                  facetType: 'checkboxlist',
                  margin: '5 10 10 5',
                  padding: '0 10 5 10',
                  title: tr('Provider'),
                  collapsible: true,
                  height: 80,
                  overflowY: 'auto'
               },
               { xtype: 'fieldset',
                  id: 'facet1',
                  facetType: 'checkboxlist',
                  margin: '5 10 5 5',
                  padding: '0 10 5 10',
                  title: tr('Performance language'),
                  collapsible: true,                  
                  height: 80,
                  overflowY: 'auto'
               },
               { xtype: 'fieldset',
                  id: 'facet2',
                  facetType: 'tree',
                  margin: '5 10 5 5',
                  padding: '5 10 5 5',
                  title: tr('Educational level'),
                  collapsible: true,                  
                  height: 148,
                  overflowY: 'auto',
                  resizable: true,
                  resizeHandles: 's' 
               },
               { xtype: 'fieldset',
                  id: 'hiddenfacet3',
                  facetType: 'radiomulti',
                  margin: '5 10 5 5',
                  title: tr('Performance date'),
                  collapsible: true
               }               
            ]
        };        

        Ext.apply(this, cfg);
        this.callParent(arguments); 

        this.createFacet0();
        this.createFacet1();
        this.createFacet2();
        this.createFacet3();
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
                    var elem = { id: provider.uri, layout: 'hbox', width: 500,
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
        /*Ext.Ajax.request( {
            url: 'rest/queryEngine/performanceLanguages?lang=' + this.lang,
            method: 'GET',
            success: function( response ) {
                var languages = Ext.JSON.decode(response.responseText, true).performanceLanguages;
                for (i = 0; i < languages.length; i++) {
                    var language = languages[i];
                    var elem = { id: language.lang, layout: 'hbox', width: 500, 
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
        } );*/

        var elem = { id: 'fra', layout: 'hbox', width: 500, 
                                 items: [ { xtype: 'checkbox', boxLabel: tr('French'), 
                                            handler: this.facetedSearch, scope: this },
                                          { xtype:'label', text: '', margin: '4 0 0 5' } ] };
        facet.add(elem);
        elem = { id: 'eng', layout: 'hbox', width: 500, 
                                 items: [ { xtype: 'checkbox', boxLabel: tr('English'), 
                                            handler: this.facetedSearch, scope: this },
                                          { xtype:'label', text: '', margin: '4 0 0 5' } ] };
        facet.add(elem);
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
           if (node.getData().uri != "") {
               if (conceptProxy.reader.jsonData.notLeafConcepts != undefined)
                   this.notLeafConcepts = conceptProxy.reader.jsonData.notLeafConcepts;
               conceptProxy.url = 'rest/vocs/'+ encodeURIComponent(node.getData().uri) +'/children?lang=' + this.lang;
           }
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
            plugins: ['dvp_nodedisabled'] 

        });

        this.automaticCollapse = 0;
        tree.on( 'afteritemexpand', function(node) {  
            if (this.automaticCollapse < this.notLeafConcepts) {
                node.collapse();
                this.automaticCollapse++;
            }                   
        }, this);

        tree.on( 'checkchange', function(node) {  
            checkTreeNodes(node, node.get('checked'));
            if (!node.get('checked')) {                
                while(node != tree.getRootNode()) {
                    node.parentNode.set('checked', false);
                    node = node.parentNode;
                }
            }
            this.facetedSearch();
        }, this);


        var facet = this.getComponent(2);
        facet.add(tree);        
    },
    createFacet3: function() {
        var facet = this.getComponent(3);

        var elem = { layout: 'hbox', 
                     items: [ { xtype: 'radio', id:'fromCb', name: 'dateSearchType', checked: true, boxLabel: tr('From'), 
                                margin: '0 10 0 0', handler: this.fromSearch, scope: this },
                              { xtype: 'radio', id:'toCb', name: 'dateSearchType', boxLabel: tr('To'), 
                                margin: '0 10 0 0', handler: this.toSearch, scope: this }, 
                              { xtype: 'radio', id:'betweenCb', name: 'dateSearchType', boxLabel: tr('Between'), 
                                margin: '0 10 0 0', handler: this.betweenSearch, scope: this } ] };               
        facet.add(elem);
        elem = { xtype: 'datefield', id: 'firstDate', margin: '8 0 8 0', width: 250, editable: false };
        facet.add(elem);
        elem = { xtype: 'label', id: 'andLabel', margin: '0 0 0 5', text: tr('and'), hidden: true };
        facet.add(elem);
        elem = { xtype: 'datefield', id: 'secondDate', margin: '8 0 8 0', width: 250, hidden: true, editable: false };
        facet.add(elem);

        Ext.getCmp('firstDate').on( 'select', this.datePicked, this );
        Ext.getCmp('secondDate').on( 'select', this.datePicked, this );
    },
    fromSearch: function(cb, checked) {
        if (checked) {
            Ext.getCmp('andLabel').setVisible(false);
            Ext.getCmp('secondDate').setVisible(false);

            if (Ext.getCmp('firstDate').getValue() != null)
                this.facetedSearch();
        }
    },
    toSearch: function(cb, checked) {
        if (checked) {
            Ext.getCmp('andLabel').setVisible(false);
            Ext.getCmp('secondDate').setVisible(false);

            if (Ext.getCmp('firstDate').getValue() != null)
                this.facetedSearch();
        }
    },
    betweenSearch: function(cb, checked) {
        if (checked) {
            Ext.getCmp('andLabel').setVisible(true);
            Ext.getCmp('secondDate').setVisible(true);

            if (Ext.getCmp('firstDate').getValue() != null &&
                Ext.getCmp('secondDate').getValue() != null)
                this.facetedSearch();
        }
    },
    datePicked: function(dateField, value) {
        if (dateField == Ext.getCmp('secondDate') && 
               Ext.getCmp('firstDate').getValue() == null)
            return;
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
            if (facet.facetType == "checkboxlist") {
                var k = 0;
                for (j = 0; j < facet.items.length; j++) {
                    var comp = facet.getComponent(j);
                    if (comp.getComponent(0).getValue()) {
                        values[k] = {};
                        values[k].id = comp.getId();
                        k++;
                    }
                }
            }
            else if (facet.facetType == 'tree') {
                var root = facet.getComponent(0).getRootNode();
                treeCriterias(root, values, 0);
            }

            if (values.length > 0) {
                facetCriterias.values = values;
                criterias[c] = facetCriterias; 
                c++;
            }            
        }
        return criterias;  
    },
    updateFacets: function(facetInfos, isClear) {
        this.isUpdateProcess = true;
        for (i = 0; i < facetInfos.length; i++) {
            var infos = facetInfos[i];
            var values = infos.values;
            var facet = this.getComponent(infos.id);

            //mark all checkboxes for init
            if (facet.facetType == 'checkboxlist') {
                for (j = 0; j < facet.items.length; j++) {
                    var comp = facet.getComponent(j);
                    comp.getComponent(1).setText("x");
                }
            }
            //remove all tree nodes counts for init
            else if (facet.facetType == 'tree') {
                var root = facet.getComponent(0).getRootNode();
                clearTreeCounts(root);
            }
            //manage checkbox which have facet values
            for (j = 0; j < values.length; j++) {
                var data = values[j];
                var comp;
                if (facet.facetType == 'tree') {
                    var root = facet.getComponent(0).getRootNode();
                    comp = root.findChild('uri', data.id, true);
                }
                else
                    comp = facet.getComponent(data.id);

                if (comp != undefined) {
                    if (facet.facetType == 'checkboxlist') {
                        comp.getComponent(0).setDisabled(false);
                        comp.getComponent(1).setText('<font color="blue">[' + data.count + ']</font>', false);
                    }
                    else if (facet.facetType == 'tree') {                        
                        comp.set('label', comp.get('label') + '<font style="margin-left:5px" color="blue">[' + data.count + ']</font>');
                        comp.set('xxx', false);
                    }
                }
            }            

            //disable and uncheck others
            if (facet.facetType == 'checkboxlist') {
                for (j = 0; j < facet.items.length; j++) {
                    var comp = facet.getComponent(j);
                    if (comp.getComponent(1).text == "x") {
                        comp.getComponent(0).setValue(false);
                        comp.getComponent(0).setDisabled(true);
                        comp.getComponent(1).setText("");
                    }
                }
            }
            //disable all tree nodes with count zero
            else if (facet.facetType == 'tree') {
                var root = facet.getComponent(0).getRootNode();
                disableTreeNodes(root);
            }
        }

        this.isUpdateProcess = false;
        
        this.clearButton.setDisabled(isClear);
    },
    clearCriterias: function() {
        this.isUpdateProcess = true;

        for (i = 0; i < this.items.length; i++) {
            var facet = this.getComponent(i);
            if (!facet.getId().startsWith("facet"))
                continue;
            if (facet.facetType == "checkboxlist") {
                for (j = 0; j < facet.items.length; j++) {
                    var comp = facet.getComponent(j);
                    comp.getComponent(0).setValue(false);
                }
            }
            else if (facet.facetType == "tree")                
                checkTreeNodes(facet.getComponent(0).getRootNode(), false);
        }

        this.isUpdateProcess = false;

        this.facetedSearch();

        this.clearButton.setDisabled(true);
    }
});

function clearTreeCounts(node) { 
    var label = node.get('label');
    var index = label.indexOf("<font");        
    if (index != -1)
        node.set('label', label.substring(0, index));
    
    node.set('xxx', true);
    node.eachChild(function(n) {
        clearTreeCounts(n);
    });     
}

function disableTreeNodes(node) { 
    node.set('disabled', node.get('xxx')) 
    if (node.get('xxx'))
        node.set('checked', false);

    node.eachChild(function(n) {        
        disableTreeNodes(n);
    });     
}

function checkTreeNodes(node, checked) { 
    node.eachChild(function(n) { 
        if (!n.get('disabled'))
            n.set('checked', checked);        
        checkTreeNodes(n, checked);
    });     
}

function treeCriterias(node, values, k) { 
    node.eachChild(function(n) { 
        if (n.get('checked')) {
            values[k] = {};
            values[k].id = n.get('uri');
            k++;
        }  
        treeCriterias(n, values, k);
    });     
}

Ext.define('VocabularyConceptModel', {
    extend: 'Ext.data.Model',
    fields: [ 'uri', 'label' ]
});