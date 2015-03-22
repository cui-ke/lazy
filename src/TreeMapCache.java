class TreeMapCache extends java.util.TreeMap {
    
    static int globalstatPut=0,globalstatGetFound=0,globalstatGetAsk=0;
    static int globalstatPutSize=0;
    static int globalstatSaveTime=0,globalstatQueryTime=0;
    
    
    int MAXCACHE;
    Object[] circular;
    long[] saveTime;
    int idx = 0;
    int statPut=0,statGetFound=0,statGetAsk=0;
    int statPutSize=0;
    int statSaveTime=0,statQueryTime=0;
    
    String cacheName;
    
    public TreeMapCache(int max, String name) { // length of cache
        super();
        MAXCACHE = max;
        circular = new Object[MAXCACHE];
        saveTime = new long[MAXCACHE];
        cacheName = name;
    }
    
    
    public synchronized void clear() {
        circular = new Object[MAXCACHE];
        idx = 0;
        super.clear();
    }
    
    public synchronized Object put(Object sKey, Object sInfo, long execTime) {
        if(MAXCACHE>0){
            idx = (++idx % MAXCACHE);
            if (ns.verboseCache)
                System.out.println(
                "TreeMapCache:"
                + cacheName
                + " position in cache :"
                + idx
                + " add object:"
                + sKey.toString());
            if (circular[idx] != null) {
                if (ns.verboseCache)
                    System.out.println(
                    "TreeMapCache:" + cacheName + " remove object from cache :" + sKey.toString());
                super.remove(circular[idx]);
            }
            circular[idx] = sKey;
            
            if (execTime==0) execTime=1;
            saveTime[idx]= execTime;
            statPut++;
            statPutSize+=(sInfo.toString()).length();
            statQueryTime+=(int)execTime;
            globalstatPut++;
            globalstatPutSize+=(sInfo.toString()).length();
            globalstatQueryTime+=(int)execTime;
            
            return super.put(sKey, sInfo);
        }
        else { // no cache !!!
            statPut++;
            statPutSize+=(sInfo.toString()).length();
            statQueryTime+=(int)execTime;
            globalstatPut++;
            globalstatPutSize+=(sInfo.toString()).length();
            globalstatQueryTime+=(int)execTime;
            return null;
        }
    }
    
    public synchronized Object get(Object sKey) {
        Object fromcache=super.get(sKey);
        if (fromcache!=null) {
            if (ns.computeSaveTime){
                for (int i=0; i<MAXCACHE; i++){
                    if (circular[i]!=null&&circular[i].toString().equals(sKey.toString())){
                        // System.out.println("OK "+saveTime[i]);
                        statSaveTime+=saveTime[i];
                        globalstatSaveTime+=saveTime[i];
                        break;}
                    
                }
            }
            ++statGetFound;
            ++globalstatGetFound;
        }
        ++statGetAsk;
        ++globalstatGetAsk;
        
        if (ns.verboseCache)
            System.out.println(
            "TreeMapCache:" + cacheName + " look object from cache, key is:" + sKey.toString());
        //System.out.println(getGlobalStatistic());
        
        return fromcache;
    }
    public synchronized String getXMLStatistic(){
        return
        "<CELL>"+MAXCACHE+"</CELL>"+
        "<CELL>"+statPut+"</CELL>"+
        "<CELL>"+statPutSize+"</CELL>"+
        "<CELL>"+statGetAsk+"</CELL>"+
        "<CELL>"+statGetFound+"</CELL>"+
        "<CELL>"+statQueryTime+"</CELL>"+
        "<CELL>"+statSaveTime+"</CELL>"
        ;
    }
    public synchronized String getStatistic(){
        return "cache: "+cacheName+
        " Put: "+MAXCACHE+
        " Put: "+statPut+
        " PutSize: "+statPutSize+
        " Get: "+statGetAsk+
        " Found: "+statGetFound+
        " QueryTime: "+statQueryTime+
        " SaveTime: "+statSaveTime
        ;
    }
    public static synchronized String getXMLGlobalStatistic(){
        return
        "<CELL>"+"-"+"</CELL>"+
        "<CELL>"+globalstatPut+"</CELL>"+
        "<CELL>"+globalstatPutSize+"</CELL>"+
        "<CELL>"+globalstatGetAsk+"</CELL>"+
        "<CELL>"+globalstatGetFound+"</CELL>"+
        "<CELL>"+globalstatQueryTime+"</CELL>"+
        "<CELL>"+globalstatSaveTime+"</CELL>"
        ;
    }
    public static synchronized String getGlobalStatistic(){
        return "For all caches:" +
        " Put: "+globalstatPut+
        " PutSize: "+globalstatPutSize+
        " Get: "+globalstatGetAsk+
        " Found: "+globalstatGetFound+
        " QueryTime: "+globalstatQueryTime+
        " SaveTime: "+globalstatSaveTime
        ;
    }
}
