package cloudify.widget.hpcloud;


/**
 * HP implementation of CloudCreateServerOptions
 * @author evgenyf
 * Date: 10/7/13
 */
public class HPCloudCreateServerOptions{}
//implements CloudCreateServerOptions {
//
//	private final CreateServerOptions createServerOptions;
//
//	public HPCloudCreateServerOptions( Conf conf ){
//
//        CreateServerOptions serverOptions = new CreateServerOptions();
//
//        Map<String,String> metadata = new HashMap<String, String>();
//
//        List<String> tags = new LinkedList<String>();
//
//        if( !StringUtils.isEmpty( conf.server.bootstrap.tags ) ){
//            tags.add( conf.server.bootstrap.tags );
//        }
//
//        metadata.put("tags", StringUtils.join(tags, ","));
//        serverOptions.metadata(metadata);
//        serverOptions.keyPairName( conf.server.bootstrap.keyPair );
//        serverOptions.securityGroupNames(conf.server.bootstrap.securityGroup);
//
//        this.createServerOptions = serverOptions;
//	}
//
//	public CreateServerOptions getCretaeServerOptions(){
//		return createServerOptions;
//	}
//
//	@Override
//	public String toString() {
//		return "HPCloudCreateServerOptions [createServerOptions=" + createServerOptions + "]";
//	}
//}