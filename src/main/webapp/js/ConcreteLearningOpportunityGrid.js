Ext.define( 'Proeaf.ConcreteLearningOpportunityGrid', {
    extend: 'Ext.grid.Panel',
    initComponent: function (config) { 

        Ext.define('ConcreteLearninOpportunityModel', {
            extend: 'Ext.data.Model',
            fields: [ 'uri', 'start', 'duration', 'deliveryMode', 'perfLanguage' ]
        });

        this.proxy = Ext.create('Ext.data.proxy.Ajax', {
            reader: {
                root: 'learningOpportunities'
            }
        });

        this.cloStore = Ext.create('Ext.data.JsonStore', {
            model: 'ConcreteLearninOpportunityModel',
            proxy: this.proxy
        });

        this.start = function( value ) {
            if (this.dateCriterias == -1)   {             
                var criterias = searchManager.getDateCriterias();
                this.dateCriterias = (criterias == null)?'none':criterias;                
            }
            if (this.dateCriterias == "none")
                return formatISODate(value, this.lang);
            else {
                var date = new Date(value);
                var from = null;
                if (this.dateCriterias.from != undefined)
                    from = new Date(this.dateCriterias.from + "T00:00:00");
                var to = null;
                if (this.dateCriterias.to != undefined)
                    to = new Date(this.dateCriterias.to + "T00:00:00");
                var dateFit = false;
                if (from != null && to != null)
                    dateFit = (date > from && date < to);
                else if (from != null)
                    dateFit = (date > from);
                else if (to != null)
                    dateFit = (date < to);
 
                var resp = formatISODate(value, this.lang);
                if (dateFit)
                    resp = '<b>' + resp + '</b>';

                return resp;
            }
        };

        this.duration = function( value ) {
            return formatISODuration(value);
        };

        this.renderPerfLang = function( value ) {
            return tr(getlanguage( value ));
        };
       
        var ctxtMenu = Ext.create('Ext.menu.Menu', {
           items:[ {
               text: tr('Last minute infos'),
               icon: 'images/clock.png',
               handler: this.manageLastMinuteInfos,
               scope: this
           } ]
        });

        cfg = {
            store: this.cloStore,
            columns: [ 
                { text: 'uri', dataIndex: 'uri', hidden: true },
                { text: tr('Date'), flex: 1, sortable: true, xtype: 'datecolumn', dataIndex: 'start', renderer:this.start },
                { text: tr('Duration'), width: 60, sortable: true, xtype: 'datecolumn', dataIndex: 'duration', renderer:this.duration  },
                { text: tr('Diffusion'), width: 110, sortable: true, dataIndex: 'deliveryMode' },
                { text: tr('Language'),  width: 80, sortable: true, dataIndex: 'perfLanguage', renderer: this.renderPerfLang }
            ],          
            viewConfig: {
                loadingText: tr('Search in progress') + '...',
                stripeRows: false
            },
            listeners: {
                el: {
                    contextmenu: {                         
                        fn: function(e) { 
                            e.stopEvent(); // stops the default browser event.
                            ctxtMenu.showAt(e.getXY());
                            return false;
                        }
                    }
                }
            }            
        };
        Ext.apply(this, cfg);
        this.callParent(arguments);   
    },
    init: function(glo) {
        this.dateCriterias = -1;
        this.proxy.url = 'rest/queryEngine/concreteLOs?glo=' + glo + '&lang=' + this.lang;
        this.cloStore.load();
    },
    clear: function() {          
        this.cloStore.loadRawData([]);
    },
    manageLastMinuteInfos: function() {        
        var selectedRecord = this.getSelectionModel().getSelection()[0];
        this.parent.manageLastMinuteInfos(selectedRecord.data.uri);
    }
} );


