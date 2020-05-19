---
layout: post
title: "java"
description: "java"
category: [java]
tags: [loadingCache]
---
{% include JB/setup %}

```

<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>19.0</version>
</dependency>
```



```
class Test{
		
		private HosRecipeMedicineInfo fetch(String hosCode) {
        List<String> recipeItemCodes = recipeMedicineDao.query(hosCode);
        return new HosRecipeMedicineInfo()
                .setHosCode(hosCode)
                .setCodes(recipeItemCodes);
    }
    
		private LoadingCache<String, HosRecipeMedicineInfo> hosRecipeMedicineCache = CacheBuilder.newBuilder()
            // 30秒刷新一次药品本地缓存
            .expireAfterWrite(30, TimeUnit.SECONDS)
            .build(new CacheLoader<String, HosRecipeMedicineInfo>() {
                @Override
                public HosRecipeMedicineInfo load(String hosCode) throws Exception {
                    return fetch(hosCode);
                }
            });
            
    public List<HosRecipeMedicineInfo> listCodes(List<HosRecipeMedicineInfo> hosRecipeMedicineInfos) {
        return Safes.of(hosRecipeMedicineInfos).stream()
                .map(info -> {
                    // hosCode和codes字段必传，这里是为了兜底
                    if (StringUtils.isBlank(info.getHosCode())
                            || CollectionUtils.isEmpty(info.getCodes())) {
                        return null;
                    }
                    HosRecipeMedicineInfo cacheInfo = null;
                    try {
                        cacheInfo = hosRecipeMedicineCache.get(info.getHosCode());
                    } catch (ExecutionException e) {
                        logger.error("查询医院药品缓存失败", e);
                    }
                    if (cacheInfo == null) {
                        return null;
                    }
                    // 求交集，结果保存在info中
                    info.getCodes().retainAll(cacheInfo.getCodes());
                    return info;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
```

