import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from 'app/app.module';
import { environment } from 'environments/environment';
import { environmentLoader as environmentLoaderPromise } from './environments/environmentLoader';


environmentLoaderPromise.then(env => {

  environment.production = env.production;
  environment.api_url = env.api_url;
  environment.kpi_api_url = env.kpi_api_url;
  environment.mapApiKey = env.mapApiKey;

  if (environment.production) {
    enableProdMode();
  }

  const bootstrapPromise = platformBrowserDynamic().bootstrapModule(AppModule);

  //Logging bootstrap information
  bootstrapPromise.then(success => console.log(`Bootstrap success`))
    .catch(err => console.error(err));
});
