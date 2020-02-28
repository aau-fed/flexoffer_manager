import { ConfigurationModule } from './configuration.module';

describe('ConfigurationModule', () => {
  let configurationModule: ConfigurationModule;

  beforeEach(() => {
    configurationModule = new ConfigurationModule();
  });

  it('should create an instance', () => {
    expect(configurationModule).toBeTruthy();
  });
});
