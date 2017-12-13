package com.example.stackmasterdetailfrag.util;

import android.support.annotation.NonNull;

import com.example.stackmasterdetailfrag.paths.MasterDetailPath;
import com.zhuinden.simplestack.BackstackManager;
import com.zhuinden.simplestack.SavedState;
import com.zhuinden.simplestack.StateChange;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Owner on 2017. 03. 03..
 */
public class MasterDetailStateClearStrategy
        implements BackstackManager.StateClearStrategy {
    @Override
    public void clearStatesNotIn(@NonNull Map<Object, SavedState> keyStateMap, @NonNull StateChange stateChange) {
        Set<Object> keys = keyStateMap.keySet();
        Iterator<Object> keyIterator = keys.iterator();
        while(keyIterator.hasNext()) {
            Object key = keyIterator.next();
            boolean isMasterOf = false;
            List<Object> newState = stateChange.getNewState();
            for(Object newKey : newState) {
                if(newKey instanceof MasterDetailPath) {
                    if(key.equals(((MasterDetailPath) newKey).getMaster())) {
                        isMasterOf = true;
                        break;
                    }
                }
            }
            if(!newState.contains(key) && !isMasterOf) {
                keyIterator.remove();
            }
        }
    }
}
