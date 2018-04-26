package services;

import com.daanpanis.injection.Service;
import com.daanpanis.injection.Singleton;

@Service(parent = ServiceNoParent.class)
@Singleton
public class ServiceWithParentSingleton extends ServiceNoParent {}
