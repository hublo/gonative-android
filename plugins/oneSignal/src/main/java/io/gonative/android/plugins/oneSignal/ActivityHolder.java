package io.gonative.android.plugins.oneSignal;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

public class ActivityHolder {
    
    private List<Activity> activitiesToNotify;
    
    public ActivityHolder() {
        activitiesToNotify = new ArrayList<>();
    }
    
    
    public void addActivity(Activity activity) {
        activitiesToNotify.add(activity);
    }
    
    
    public List<Activity> getActivitiesToNotify() {
        return activitiesToNotify;
    }
    
    public void setActivitiesToNotify(List<Activity> activitiesToNotify) {
        this.activitiesToNotify = activitiesToNotify;
    }
}
