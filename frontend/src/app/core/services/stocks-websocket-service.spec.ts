import { TestBed } from '@angular/core/testing';

import { StocksWebsocketService } from './stocks-websocket-service';

describe('StocksWebsocketService', () => {
  let service: StocksWebsocketService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StocksWebsocketService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
