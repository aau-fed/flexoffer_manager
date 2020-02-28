import { HistoricalLoadModule } from './historical-load.module';

describe('HistoricalLoadModule', () => {
  let historicalLoadModule: HistoricalLoadModule;

  beforeEach(() => {
    historicalLoadModule = new HistoricalLoadModule();
  });

  it('should create an instance', () => {
    expect(historicalLoadModule).toBeTruthy();
  });
});
