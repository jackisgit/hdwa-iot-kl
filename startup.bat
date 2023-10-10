title IEPC_KL
chcp 65001
for %%j in (iot*.jar) do  java -javaagent:%%j -jar -Dfile.encoding=utf-8 -Dspring.profiles.active=qiqihaer  %%j