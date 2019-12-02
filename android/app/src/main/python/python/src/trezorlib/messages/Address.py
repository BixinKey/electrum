# Automatically generated by pb2py
# fmt: off
from .. import protobuf as p

if __debug__:
    try:
        from typing import Dict, List, Optional
        from typing_extensions import Literal  # noqa: F401
    except ImportError:
        Dict, List, Optional = None, None, None  # type: ignore


class Address(p.MessageType):
    MESSAGE_WIRE_TYPE = 30

    def __init__(
        self,
        address: str = None,
    ) -> None:
        self.address = address

    @classmethod
    def get_fields(cls) -> Dict:
        return {
            1: ('address', p.UnicodeType, 0),  # required
        }
