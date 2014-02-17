echo "updating hosts file"
LINE="127.0.0.1 `hostname`"
if [ `cat /etc/hosts | grep "$LINE" | wc -l` -gt 0 ];then
    echo "hosts file is already updated"
else
    echo $LINE >> /etc/hosts
fi
