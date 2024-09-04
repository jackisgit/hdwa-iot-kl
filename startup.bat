title IEPC_KL
chcp 65001
for %%j in (iot*.jar) do  java -jar -Dfile.encoding=utf-8 -Dspring.profiles.active=lianyungang_haizhou  %%j