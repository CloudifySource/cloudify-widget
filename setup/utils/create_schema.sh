echo "creating DB scheme"
bin/migrate_db.sh create
echo "127.0.0.1 `hostname`" >> /etc/hosts