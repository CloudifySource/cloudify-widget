'use strict';


angular.module('WidgetApp').service('WidgetShareSourcesService', function( $q ){


    // guy - another level of abstraction as we need to refer to IDs in Widget.js
    var sourcesIds = {
        'TWITTER' : 'TWITTER',
        'FACEBOOK' :'FACEBOOK',
        'GOOGLE_PLUS' : 'GOOGLE_PLUS',
        'EMBED_CODE' : 'EMBED_CODE',
        'LINKEDIN' : 'LINKEDIN',
        'RENREN' : 'RENREN',
        'SINA' : 'SINA',
        'WEIBO' : 'WEIBO',
        'WECHAT' : 'WECHAT'

    };

    this.SourcesIds = sourcesIds;

    // list of sources and they are active by default or not.
    // active defatul value is set by backward compatibility
    var sources = [
        {
            'id' : sourcesIds.TWITTER,
            'label' : 'Twitter',
            'active' : true

        },
        {
            'id' : sourcesIds.FACEBOOK,
            'label' : 'Facebook',
            'active' : true
        },
        {
            'id' : sourcesIds.GOOGLE_PLUS,
            'label' : 'Google+',
            'active' : true
        },
        {
            'id' : sourcesIds.EMBED_CODE,
            'label' : 'Embed Code',
            'active' : true
        },
        {
            'id' : sourcesIds.LINKEDIN,
            'label' : 'Linkedin',
            'active' : false
        },
        {
            'id' : sourcesIds.RENREN,
            'label' : 'Renren',
            'active' : false
        },
//        {
//            'id' : 'DOUBAN',
//            'label' : 'Douban',
//            'active' : false
//        },
        {
            'id' : sourcesIds.SINA,
            'label' : 'Sina Tencent',
            'active' : false
        },
        {
            'id' : sourcesIds.WEIBO,
            'label' : 'Weibo Tencent',
            'active': false
        }//,
//        {
//            'id' : 'QZONE',
//            'label' : 'Qzone',
//            'active' : false
//        },
//        {
//            'id' : sourcesIds.WECHAT, // guy no share button feature
//            'label' : 'Wechat',
//            'active': false
//        }
    ];

    this.getShareSources = function(){
        var deferred = $q.defer();
        deferred.resolve( { 'data' : sources } );
        return deferred.promise;
    };

    this.getById = function( id ){
        return _.find(sources, {'id' : id});
    };


    // gets a source and turns it into a model item.
    function sourceToItem( source ){
        return _.pick(source,['id','active']);
    }

    // don't use get default, because we are dealing with a list of values.
//    this.getDefault= function(){
//        return _.map(sources, function(item){ return sourceToItem(item); });
//    };

    // receives a list,and adds to it missing social networks with their default values.
    this.updateSocialSources = function( items ){
        // add missing items from system to model
        _.each( sources, function(source){
            var found = _.find( items, {'id' : source.id });
            if ( !found ){
                items.push(sourceToItem(source));
            }
        });


        // remove delete items from system in model
        _.remove(items, function( item ){

            return !_.find(sources, {'id' : item.id });
        });

    };

});