// onesignal
gonative.onesignal = {
    run: {
        onesignalInfo: function(){
            addCommand("gonative://run/gonative_onesignal_info");
        }
    },
    onesignalInfo: function(params){
        return addCommandCallback("gonative://run/gonative_onesignal_info", params, true);
    },
    register: function (){
        addCommand("gonative://onesignal/register");
    },
    userPrivacyConsent:{
        grant: function (){
            addCommand("gonative://onesignal/userPrivacyConsent/grant");
        },
        revoke: function (){
            addCommand("gonative://onesignal/userPrivacyConsent/revoke");
        }
    },
    tags: {
        getTags: function(params){
            return addCommandCallback("gonative://onesignal/tags/get", params);
        },
        setTags: function (params){
            addCommand("gonative://onesignal/tags/set", params);
        }
    },
    showTagsUI: function (){
        addCommand("gonative://onesignal/showTagsUI");
    },
    promptLocation: function (){
        addCommand("gonative://onesignal/promptLocation");
    },
    iam: {
        addTrigger: function (triggers){
            if(triggers){
                var keyLocal = Object.keys(triggers)[0];
                var params = {
                    key: keyLocal,
                    value: triggers[keyLocal]
                };
                addCommand("gonative://onesignal/iam/addTrigger", params);
            }
        },
        addTriggers: function (params){
            addCommand("gonative://onesignal/iam/addTriggers", params);
        },
        removeTriggerForKey: function (key){
            var params = {key: key};
            addCommand("gonative://onesignal/iam/removeTriggerForKey", params);
        },
        getTriggerValueForKey: function (key){
            var params = {key: key};
            addCommand("gonative://onesignal/iam/getTriggerValueForKey", params);
        },
        pauseInAppMessages: function (){
            addCommand("gonative://onesignal/iam/pauseInAppMessages?pause=true");
        },
        resumeInAppMessages: function (){
            addCommand("gonative://onesignal/iam/pauseInAppMessages?pause=false");
        },
        setInAppMessageClickHandler: function (handler){
            var params = {handler: handler};
            addCommand("gonative://onesignal/iam/setInAppMessageClickHandler", params);
        }
    },
    externalUserId: {
        set: function (params){
            addCommand("gonative://onesignal/externalUserId/set", params);
        },
        remove: function(){
            addCommand("gonative://onesignal/externalUserId/remove")
        }
    }
};
