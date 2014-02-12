echo "installing ruby"
yum install -y ruby ruby-devel rubygems gcc make libxml2 libxml2-devel libxslt libxslt-devel
#curl -sL https://docs.hpcloud.com/file/hpfog-0.0.19.gem >hpfog-0.0.19.gem
#curl -sL https://docs.hpcloud.com/file/hpcloud-1.6.0.gem >hpcloud-1.6.0.gem
#echo "installing hpcloud cli"
#gem install --no-rdoc --no-ri hpfog-0.0.19.gem hpcloud-1.6.0.gem

echo "installing sass"
gem install sass
