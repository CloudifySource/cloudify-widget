update server_node set lead_id = null;
alter table server_node add unique(lead_id);