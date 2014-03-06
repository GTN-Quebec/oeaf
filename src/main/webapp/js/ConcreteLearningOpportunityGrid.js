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
            return formatISODate(value, this.lang);
        };

        this.duration = function( value ) {
            return formatISODuration(value);
        };

        this.renderPerfLang = function( value ) {
            return tr(getlanguage( value ));
        };
       
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
            }            
        };
        Ext.apply(this, cfg);
        this.callParent(arguments);   
    },
    init: function(glo) {
        this.proxy.url = 'rest/queryEngine/concreteLOs?glo=' + glo + '&lang=' + this.lang;
        this.cloStore.load();
    },
    clear: function() {        
        this.cloStore.loadRawData([]);
    }  
} );


