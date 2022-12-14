#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

__author__ = 'Marko A. Rodriguez (http://markorodriguez.com)'

from gremlin_python import statics
from gremlin_python.process.traversal import Cardinality
from gremlin_python.process.traversal import P
from gremlin_python.process.traversal import Pop


class TestStatics(object):
    def test_enums(self):
        statics.load_statics(globals())
        assert isinstance(list_, Cardinality)
        assert list_ is Cardinality.list_
        #
        assert isinstance(eq(2), P)
        assert eq(2) == P.eq(2)
        #
        assert isinstance(first, Pop)
        assert first == Pop.first
        statics.unload_statics(globals())

    def test_singlebyte(self):
        assert -128 == statics.SingleByte(-128)
        assert 1 == statics.SingleByte(1)
        assert 127 == statics.SingleByte(127)
        try:
            statics.SingleByte(128)
            raise Exception("SingleByte should throw a value error if input is larger than 127")
        except ValueError:
            pass

        try:
            statics.SingleByte(-129)
            raise Exception("SingleByte should throw a value error if input is smaller than -128")
        except ValueError:
            pass

    def test_singlechar(self):
        assert 'a' == statics.SingleChar('a')
        assert chr(76) == statics.SingleChar(chr(76))
        assert chr(57344) == statics.SingleChar(chr(57344))
        try:
            statics.SingleChar('abc')
            raise Exception("SingleChar should throw a value error if input is not a single character string")
        except ValueError:
            pass
