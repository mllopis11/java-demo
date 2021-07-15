package mike.demo.dataloader.domain;

import java.nio.file.Path;

import mike.bootstrap.utilities.helpers.Resource;
import mike.demo.dataloader.model.LoaderDescriptor;

public class LoaderDescriptorFactory {

    private LoaderDescriptorFactory() {}
    
    public static LoaderDescriptor newLoaderDescriptor(Path file) {
        return LoaderDescriptorFactory.newLoaderDescriptor(new Resource(file));
    }
    
    public static LoaderDescriptor newLoaderDescriptor(String filename) {
        return LoaderDescriptorFactory.newLoaderDescriptor(new Resource(filename));
    }
    
    public static LoaderDescriptor newLoaderDescriptor(Resource resource) {
        return new LoaderDescriptorReader(resource).read();
    }
}
