package com.bigdata.platform.scheduler.web.core.utils;

import java.util.*;

public class JobDependsCycleCheck {
    private Map<Long, Set<Long>> jobOnlineMap;
    private Set<Long> visitJobIds = new HashSet<>();

    public JobDependsCycleCheck(Map<Long, Set<Long>> jobOnlineMap) {
        this.jobOnlineMap = jobOnlineMap;
    }

    private boolean iterCheck(Long nodeId, Long parentId) {
        Set<Long> path = new HashSet<>();
        path.add(nodeId);
        path.add(parentId);
        Queue<Long> parentQueue = new LinkedList<>();
        Queue<Set<Long>> pathQueue = new LinkedList<>();
        parentQueue.offer(parentId);
        pathQueue.offer(path);
        while (!parentQueue.isEmpty()) {
            parentId = parentQueue.poll();
            path = pathQueue.poll();
            if (jobOnlineMap.containsKey(parentId) && null != jobOnlineMap.get(parentId)) {
                for (Long newParentId: jobOnlineMap.get(parentId)) {
                    if (path.contains(newParentId)) {
                        return true;
                    }
                    parentQueue.offer(newParentId);
                    Set<Long> newPath = new HashSet<>(path);
                    newPath.add(newParentId);
                    pathQueue.offer(newPath);
                }
            }
        }
        return false;
    }

    public boolean checkCycle(Long id, List<Long> parentIds) {
        for (Long parentId : parentIds) {
            boolean result = iterCheck(id, parentId);
            if (result) {
                return true;
            }
        }
        return false;
    }
}
