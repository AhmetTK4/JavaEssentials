"""Running app: python3 scripts/smoke.py [http://localhost:8080]."""
import json
import sys
from urllib.request import urlopen
from urllib.error import HTTPError

base = sys.argv[1] if len(sys.argv) > 1 else 'http://localhost:8080'
def get(path):
    with urlopen(base + '/api/items/' + path, timeout=5) as response:
        assert response.status == 200
        return json.load(response)

catalog = get('catalog?maxPrice=300')
assert catalog == [
    {'id': 3, 'name': 'Desk', 'price': 300},
    {'id': 5, 'name': 'Monitor', 'price': 300},
    {'id': 6, 'name': 'Notebook', 'price': 5},
]
assert get('inventory') == {
    'Electronics': {'itemCount': 3, 'totalUnits': 7, 'inventoryValue': 4800},
    'Furniture': {'itemCount': 1, 'totalUnits': 5, 'inventoryValue': 1500},
    'Stationery': {'itemCount': 1, 'totalUnits': 10, 'inventoryValue': 50},
}
availability = get('availability')
assert [i['id'] for i in availability['available']] == [1, 3, 5, 6]
assert [i['id'] for i in availability['unavailable']] == [2]
assert [i['id'] for i in get('top?limit=3')] == [1, 3, 5]
assert get('tags') == ['accessory', 'display', 'home', 'portable', 'work']
for path in ('top?limit=0', 'catalog?maxPrice=-1', 'top?limit=abc'):
    try:
        get(path)
    except HTTPError as error:
        assert error.code == 400
    else:
        raise AssertionError('Expected HTTP 400: ' + path)
print('PASS: 5 endpoint responses and 3 invalid requests')
