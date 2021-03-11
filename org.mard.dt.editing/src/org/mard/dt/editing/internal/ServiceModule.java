/**
 *
 */
package org.mard.dt.editing.internal;

import org.mard.dt.editing.IPathEditingService;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 * @author Dmitriy Marmyshev
 *
 */
public class ServiceModule
    extends AbstractModule
{

    @Override
    protected void configure()
    {
        bind(IPathEditingService.class).to(ProjectPathEditingService.class).in(Singleton.class);
    }

}
