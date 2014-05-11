package com.hongqi.findmeHttp;

import java.util.*;
import com.hongqi.findmeHttp.HttpUtil;
public class getFriendList {

    public List<Map<String, Object>> getFriendList(String inputInfo){
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        String uid = "";
        String username = "";
        String online = "";
        char a[] = inputInfo.toCharArray();
        int i = 0;

        while (i < a.length){
            uid = "";
            username ="";
            online = "";
            while(i < a.length && a[i] != ','){
                uid += a[i];
                i++;
            }
            i++;
            while(i < a.length && a[i] != ','){
                username += a[i];
                i++;
            }
            i++;
            while(i < a.length && a[i] != '|')  {
                online += a[i];
                i++;
            }
            i++;
            System.out.println(online);
            if (online.equals("true")){
                online = "在线";
            }
            else if (online.equals("false")){
                online = "离线";
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("uid", uid);
            map.put("name", username);
            map.put("online", online);
            list.add(map);
        }
        return list;

    }
    public Map<String,Object>  getFriendLocal(String Friendlocal){
        Friendlocal = HttpUtil.getFriendLocal(Friendlocal);
        String Friendlocalx = "";
        String Friendlocaly = "";
        char a[] = Friendlocal.toCharArray();
        int i = 0;
        while(i < a.length && a[i] != ','){
            Friendlocalx += a[i];
            i++;
        }
        i++;
        while(i < a.length) {
            Friendlocaly += a[i];
            i++;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("x", Friendlocalx);
        map.put("y", Friendlocaly);
        return map;
    }
}
