import { TestBed } from '@angular/core/testing';

import { HistoricalLoadService } from './historical-load.service';

describe('HistoricalLoadService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: HistoricalLoadService = TestBed.get(HistoricalLoadService);
    expect(service).toBeTruthy();
  });
});
