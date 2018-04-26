package services;

import com.daanpanis.injection.Service;

@Service(parent = ServiceNoParent.class)
public class ServiceWithParent extends ServiceNoParent {


}
